package com.dew.ed828.aihuaPlayer.player.playback

import android.support.v4.util.ArraySet
import android.util.Log
import com.dew.ed828.aihuaPlayer.about.BuildConfig.DEBUG
import com.dew.ed828.aihuaPlayer.about.R.string.playlist
import com.dew.ed828.aihuaPlayer.player.mediasource.FailedMediaSource
import com.dew.ed828.aihuaPlayer.player.mediasource.LoadedMediaSource
import com.dew.ed828.aihuaPlayer.player.mediasource.ManagedMediaSource
import com.dew.ed828.aihuaPlayer.player.mediasource.ManagedMediaSourcePlaylist
import com.dew.ed828.aihuaPlayer.player.playqueue.PlayQueue
import com.dew.ed828.aihuaPlayer.player.playqueue.PlayQueueItem
import com.dew.ed828.aihuaPlayer.player.playqueue.event.*
import com.dew.ed828.aihuaPlayer.util.ServiceHelper

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.subscriptions.EmptySubscription
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 *
 * Created by Edward on 12/5/2018.
 *
 */

class MediaSourceManager private constructor(
    private val playbackListener: PlaybackListener,
    private val playQueue: PlayQueue,
    /**
     * Process only the last load order when receiving a stream of load orders (lessens I/O).
     * <br></br><br></br>
     * The higher it is, the less loading occurs during rapid noncritical timeline changes.
     * <br></br><br></br>
     * Not recommended to go below 100ms.
     *
     * @see .loadDebounced
     */
    private val loadDebounceMillis: Long,
    /**
     * Determines the gap time between the playback position and the playback duration which
     * the [.getEdgeIntervalSignal] begins to request loading.
     *
     * @see .progressUpdateIntervalMillis
     *
     */
    private val playbackNearEndGapMillis: Long,
    /**
     * Determines the interval which the [.getEdgeIntervalSignal] waits for between
     * each request for loading, once [.playbackNearEndGapMillis] has reached.
     */
    private val progressUpdateIntervalMillis: Long
) {
    private val TAG = "MediaSourceManager@" + hashCode()
    private val nearEndIntervalSignal: Observable<Long>
    private val debouncedLoader: Disposable
    private val debouncedSignal: PublishSubject<Long>

    private var playQueueReactor: Subscription
    private val loaderReactor: CompositeDisposable
    private val loadingItems: MutableSet<PlayQueueItem>

    private val isBlocked: AtomicBoolean

    private var playlist: ManagedMediaSourcePlaylist

    ///////////////////////////////////////////////////////////////////////////
    // Event Reactor
    ///////////////////////////////////////////////////////////////////////////

    private val reactor: Subscriber<PlayQueueEvent>
        get() = object : Subscriber<PlayQueueEvent> {
            override fun onSubscribe(subscription: Subscription) {
                playQueueReactor.cancel()
                playQueueReactor = subscription
                playQueueReactor.request(1)
            }

            override fun onNext(playQueueMessage: PlayQueueEvent) {
                onPlayQueueChanged(playQueueMessage)
            }

            override fun onError(e: Throwable) {}

            override fun onComplete() {}
        }

    ///////////////////////////////////////////////////////////////////////////
    // Playback Locking
    ///////////////////////////////////////////////////////////////////////////

    private val isPlayQueueReady: Boolean
        get() {
            val isWindowLoaded = playQueue.size() - playQueue.index > WINDOW_SIZE
            return playQueue.isComplete || isWindowLoaded
        }

    private val isPlaybackReady: Boolean
        get() {
            if (playlist.size() != playQueue.size()) return false

            val mediaSource = playlist.get(playQueue.index) ?: return false

            val playQueueItem = playQueue.item
            return mediaSource.isStreamEqual(playQueueItem!!)
        }

    ///////////////////////////////////////////////////////////////////////////
    // MediaSource Loading
    ///////////////////////////////////////////////////////////////////////////

    private val edgeIntervalSignal: Observable<Long>
        get() = Observable.interval(progressUpdateIntervalMillis, TimeUnit.MILLISECONDS)
            .filter { ignored -> playbackListener.isApproachingPlaybackEdge(playbackNearEndGapMillis) }

    constructor(
        listener: PlaybackListener,
        playQueue: PlayQueue
    ) : this(
        listener, playQueue, /*loadDebounceMillis=*/400L,
        /*playbackNearEndGapMillis=*/TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS),
        /*progressUpdateIntervalMillis*/TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS)
    ) {
    }

    init {
        if (playQueue.broadcastReceiver == null) {
            throw IllegalArgumentException("Play Queue has not been initialized.")
        }
        if (playbackNearEndGapMillis < progressUpdateIntervalMillis) {
            throw IllegalArgumentException(
                "Playback end gap=[" + playbackNearEndGapMillis +
                        " ms] must be longer than update interval=[ " + progressUpdateIntervalMillis +
                        " ms] for them to be useful."
            )
        }
        this.nearEndIntervalSignal = edgeIntervalSignal
        this.debouncedSignal = PublishSubject.create()
        this.debouncedLoader = getDebouncedLoader()

        this.playQueueReactor = EmptySubscription.INSTANCE
        this.loaderReactor = CompositeDisposable()

        this.isBlocked = AtomicBoolean(false)

        this.playlist = ManagedMediaSourcePlaylist()

        this.loadingItems = Collections.synchronizedSet(ArraySet())

        playQueue.broadcastReceiver!!
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(reactor)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Exposed Methods
    ///////////////////////////////////////////////////////////////////////////
    /**
     * Dispose the manager and releases all message buses and loaders.
     */
    fun dispose() {
        if (DEBUG) Log.d(TAG, "dispose() called.")

        debouncedSignal.onComplete()
        debouncedLoader.dispose()

        playQueueReactor.cancel()
        loaderReactor.dispose()
    }

    private fun onPlayQueueChanged(event: PlayQueueEvent) {
        if (playQueue.isEmpty && playQueue.isComplete) {
            playbackListener.onPlaybackShutdown()
            return
        }

        // Event specific action
        when (event.type()) {
            PlayQueueEventType.INIT, PlayQueueEventType.ERROR -> {
                maybeBlock()
                populateSources()
            }
            PlayQueueEventType.APPEND -> populateSources()
            PlayQueueEventType.SELECT -> maybeRenewCurrentIndex()
            PlayQueueEventType.REMOVE -> {
                val removeEvent = event as RemoveEvent
                playlist.remove(removeEvent.removeIndex)
            }
            PlayQueueEventType.MOVE -> {
                val moveEvent = event as MoveEvent
                playlist.move(moveEvent.fromIndex, moveEvent.toIndex)
            }
            PlayQueueEventType.REORDER -> {
                // Need to move to ensure the playing index from play queue matches that of
                // the source timeline, and then window correction can take care of the rest
                val reorderEvent = event as ReorderEvent
                playlist.move(
                    reorderEvent.fromSelectedIndex,
                    reorderEvent.toSelectedIndex
                )
            }
            PlayQueueEventType.RECOVERY -> {
            }
            else -> {
            }
        }

        // Loading and Syncing
        when (event.type()) {
            PlayQueueEventType.INIT, PlayQueueEventType.REORDER, PlayQueueEventType.ERROR, PlayQueueEventType.SELECT -> loadImmediate() // low frequency, critical events
            PlayQueueEventType.APPEND, PlayQueueEventType.REMOVE, PlayQueueEventType.MOVE, PlayQueueEventType.RECOVERY -> loadDebounced() // high frequency or noncritical events
            else -> loadDebounced()
        }

        if (!isPlayQueueReady) {
            maybeBlock()
            playQueue.fetch()
        }
        playQueueReactor.request(1)
    }

    private fun maybeBlock() {
        if (DEBUG) Log.d(TAG, "maybeBlock() called.")

        if (isBlocked.get()) return

        playbackListener.onPlaybackBlock()
        resetSources()

        isBlocked.set(true)
    }

    private fun maybeUnblock() {
        if (DEBUG) Log.d(TAG, "maybeUnblock() called.")

        if (isBlocked.get()) {
            isBlocked.set(false)
            playbackListener.onPlaybackUnblock(playlist.parentMediaSource)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Metadata Synchronization
    ///////////////////////////////////////////////////////////////////////////

    private fun maybeSync() {
        if (DEBUG) Log.d(TAG, "maybeSync() called.")

        val currentItem = playQueue.item
        if (isBlocked.get() || currentItem == null) return

        playbackListener.onPlaybackSynchronize(currentItem)
    }

    @Synchronized
    private fun maybeSynchronizePlayer() {
        if (isPlayQueueReady && isPlaybackReady) {
            maybeUnblock()
            maybeSync()
        }
    }

    private fun getDebouncedLoader(): Disposable =
        debouncedSignal.mergeWith(nearEndIntervalSignal)
            .debounce(loadDebounceMillis, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.single())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { timestamp -> loadImmediate() }


    private fun loadDebounced() {
        debouncedSignal.onNext(System.currentTimeMillis())
    }

    private fun loadImmediate() {
        if (DEBUG) Log.d(TAG, "MediaSource - loadImmediate() called")
        val itemsToLoad = getItemsToLoad(playQueue) ?: return

        // Evict the previous items being loaded to free up memory, before start loading new ones
        maybeClearLoaders()

        maybeLoadItem(itemsToLoad.center)
        for (item in itemsToLoad.neighbors) {
            maybeLoadItem(item)
        }
    }

    private fun maybeLoadItem(item: PlayQueueItem) {
        if (DEBUG) Log.d(TAG, "maybeLoadItem() called.")
        if (playQueue.indexOf(item) >= playlist.size()) return

        if (!loadingItems.contains(item) && isCorrectionNeeded(item)) {
            if (DEBUG)
                Log.d(
                    TAG, "MediaSource - Loading=[" + item.title +
                            "] with url=[" + item.url + "]"
                )

            loadingItems.add(item)
            val loader = getLoadedMediaSource(item)
                .observeOn(AndroidSchedulers.mainThread())
                /* No exception handling since getLoadedMediaSource guarantees nonnull return */
                .subscribe { mediaSource -> onMediaSourceReceived(item, mediaSource) }
            loaderReactor.add(loader)
        }
    }

    private fun getLoadedMediaSource(stream: PlayQueueItem): Single<ManagedMediaSource> =
        stream.stream.map<ManagedMediaSource> { streamInfo ->
            val source = playbackListener.sourceOf(stream, streamInfo)
            if (source == null) {
                val message = "Unable to resolve source from stream info." +
                        " URL: " + stream.url +
                        ", audio count: " + streamInfo.audioStreams.size +
                        ", video count: " + streamInfo.videoOnlyStreams.size +
                        streamInfo.videoStreams.size
                return@map FailedMediaSource(stream,
                    FailedMediaSource.MediaSourceResolutionException(
                        message
                    )
                )
            }

            val expiration = System.currentTimeMillis() + ServiceHelper.getCacheExpirationMillis(streamInfo.serviceId)
            LoadedMediaSource(source, stream, expiration)
        }.onErrorReturn { throwable ->
            FailedMediaSource(
                stream,
                FailedMediaSource.StreamInfoLoadException(throwable)
            )
        }


    private fun onMediaSourceReceived(item: PlayQueueItem, mediaSource: ManagedMediaSource) {
        if (DEBUG)
            Log.d(TAG, "MediaSource - Loaded=[${item.title}] with url=[${item.url}]")

        loadingItems.remove(item)

        val itemIndex = playQueue.indexOf(item)
        // Only update the playlist timeline for items at the current index or after.
        if (isCorrectionNeeded(item)) {
            if (DEBUG)
                Log.d(TAG, "MediaSource - Updating index=[$itemIndex] with title=[${item.title}] at url=[${item.url}]")

            playlist.update(itemIndex, mediaSource, Runnable { this.maybeSynchronizePlayer() })

        }
    }

    /**
     * Checks if the corresponding MediaSource in
     * [com.google.android.exoplayer2.source.ConcatenatingMediaSource]
     * for a given [PlayQueueItem] needs replacement, either due to gapless playback
     * readiness or playlist desynchronization.
     * <br></br><br></br>
     * If the given [PlayQueueItem] is currently being played and is already loaded,
     * then correction is not only needed if the playlist is desynchronized. Otherwise, the
     * check depends on the status (e.g. expiration or placeholder) of the
     * [ManagedMediaSource].
     */
    private fun isCorrectionNeeded(item: PlayQueueItem): Boolean {
        val index = playQueue.indexOf(item)
        val mediaSource = playlist[index]
        return mediaSource != null && mediaSource.shouldBeReplacedWith(item, /*mightBeInProgress=*/index != playQueue.index)
    }

    /**
     * Checks if the current playing index contains an expired [ManagedMediaSource].
     * If so, the expired source is replaced by a [PlaceholderMediaSource] and
     * [.loadImmediate] is called to reload the current item.
     * <br></br><br></br>
     * If not, then the media source at the current index is ready for playback, and
     * [.maybeSynchronizePlayer] is called.
     * <br></br><br></br>
     * Under both cases, [.maybeSync] will be called to ensure the listener
     * is up-to-date.
     */
    private fun maybeRenewCurrentIndex() {
        val currentIndex = playQueue.index
        val currentSource = playlist[currentIndex] ?: return

        val currentItem = playQueue.item
        if (!/*canInterruptOnRenew=*/currentSource.shouldBeReplacedWith(currentItem!!, true)) {
            maybeSynchronizePlayer()
            return
        }

        if (DEBUG)
            Log.d(TAG, "MediaSource - Reloading currently playing, index=[$currentIndex], item=[${currentItem.title}]")

        playlist.invalidate(currentIndex, Runnable { this.loadImmediate() })
    }

    private fun maybeClearLoaders() {
        if (DEBUG) Log.d(TAG, "MediaSource - maybeClearLoaders() called.")
        if (!loadingItems.contains(playQueue.item) && loaderReactor.size() > MAXIMUM_LOADER_SIZE) {
            loaderReactor.clear()
            loadingItems.clear()
        }
    }
    ///////////////////////////////////////////////////////////////////////////
    // MediaSource Playlist Helpers
    ///////////////////////////////////////////////////////////////////////////

    private fun resetSources() {
        if (DEBUG) Log.d(TAG, "resetSources() called.")
        playlist = ManagedMediaSourcePlaylist()
    }

    private fun populateSources() {
        if (DEBUG) Log.d(TAG, "populateSources() called.")
        while (playlist.size() < playQueue.size()) {
            playlist.expand()
        }
    }

    private class ItemsToLoad internal constructor(
        val center: PlayQueueItem,
        val neighbors: Collection<PlayQueueItem>
    )

    companion object {

        /**
         * Determines how many streams before and after the current stream should be loaded.
         * The default value (1) ensures seamless playback under typical network settings.
         * <br></br><br></br>
         * The streams after the current will be loaded into the playlist timeline while the
         * streams before will only be cached for future usage.
         *
         * @see .onMediaSourceReceived
         */
        private const val WINDOW_SIZE = 1

        /**
         * Determines the maximum number of disposables allowed in the [.loaderReactor].
         * Once exceeded, new calls to [.loadImmediate] will evict all disposables in the
         * [.loaderReactor] in order to load a new set of items.
         *
         * @see .loadImmediate
         * @see .maybeLoadItem
         */
        private const val MAXIMUM_LOADER_SIZE = WINDOW_SIZE * 2 + 1

        ///////////////////////////////////////////////////////////////////////////
        // Manager Helpers
        ///////////////////////////////////////////////////////////////////////////
        private fun getItemsToLoad(playQueue: PlayQueue): ItemsToLoad? {
            // The current item has higher priority
            val currentIndex = playQueue.index
            val currentItem = playQueue.getItem(currentIndex) ?: return null

            // The rest are just for seamless playback
            // Although timeline is not updated prior to the current index, these sources are still
            // loaded into the cache for faster retrieval at a potentially later time.
            val leftBound = Math.max(0, currentIndex - MediaSourceManager.WINDOW_SIZE)
            val rightLimit = currentIndex + MediaSourceManager.WINDOW_SIZE + 1
            val rightBound = Math.min(playQueue.size(), rightLimit)
            val neighbors = ArraySet(
                playQueue.streams.subList(leftBound, rightBound)
            )

            // Do a round robin
            val excess = rightLimit - playQueue.size()
            if (excess >= 0) {
                neighbors.addAll(playQueue.streams.subList(0, Math.min(playQueue.size(), excess)))
            }
            neighbors.remove(currentItem)

            return ItemsToLoad(currentItem, neighbors)
        }
    }
}
