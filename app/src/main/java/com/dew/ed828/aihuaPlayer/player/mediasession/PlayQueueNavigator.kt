package com.dew.ed828.aihuaPlayer.player.mediasession

import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.util.Util

/**
 *
 * Created by Edward on 12/6/2018.
 *
 */
class PlayQueueNavigator(private val mediaSession: MediaSessionCompat,
                         private val callback: MediaSessionCallback) : MediaSessionConnector.QueueNavigator {

    private val maxQueueSize: Int = DEFAULT_MAX_QUEUE_SIZE
    private var activeQueueItemId: Long = MediaSessionCompat.QueueItem.UNKNOWN_ID.toLong()

    override fun getSupportedQueueNavigatorActions(player: Player?): Long = ACTION_SKIP_TO_NEXT or ACTION_SKIP_TO_PREVIOUS or ACTION_SKIP_TO_QUEUE_ITEM


    override fun onTimelineChanged(player: Player) {
        publishFloatingQueueWindow()
    }

    override fun onCurrentWindowIndexChanged(player: Player) {
        if (activeQueueItemId == MediaSessionCompat.QueueItem.UNKNOWN_ID.toLong() || player.currentTimeline.windowCount > maxQueueSize) {
            publishFloatingQueueWindow()
        } else if (!player.currentTimeline.isEmpty) {
            activeQueueItemId = player.currentWindowIndex.toLong()
        }
    }

    override fun getActiveQueueItemId(player: Player?): Long {
        return callback.getCurrentPlayingIndex().toLong()
    }

    override fun onSkipToPrevious(player: Player) {
        callback.onSkipToPrevious()
    }

    override fun onSkipToQueueItem(player: Player, id: Long) {
        callback.onSkipToIndex(id.toInt())
    }

    override fun onSkipToNext(player: Player) {
        callback.onSkipToNext()
    }

    private fun publishFloatingQueueWindow() {
        if (callback.getQueueSize() == 0) {
            mediaSession.setQueue(emptyList<MediaSessionCompat.QueueItem>())
            activeQueueItemId = MediaSessionCompat.QueueItem.UNKNOWN_ID.toLong()
            return
        }

        // Yes this is almost a copypasta, got a problem with that? =\
        val windowCount = callback.getQueueSize()
        val currentWindowIndex = callback.getCurrentPlayingIndex()
        val queueSize = Math.min(maxQueueSize, windowCount)
        val startIndex = Util.constrainValue(currentWindowIndex - (queueSize - 1) / 2, 0, windowCount - queueSize)

        val queue = ArrayList<MediaSessionCompat.QueueItem>()
        for (i in startIndex until startIndex + queueSize) {
            queue.add(MediaSessionCompat.QueueItem(callback.getQueueMetadata(i), i.toLong()))
        }
        mediaSession.setQueue(queue)
        activeQueueItemId = currentWindowIndex.toLong()
    }

    override fun getCommands(): Array<String?> {
        return arrayOfNulls(0)
    }

    override fun onCommand(player: Player, command: String, extras: Bundle, cb: ResultReceiver) {

    }

    companion object {
        const val DEFAULT_MAX_QUEUE_SIZE = 10
    }
}
