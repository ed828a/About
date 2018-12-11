package com.dew.ed828.aihuaPlayer.player.mediasource

import com.dew.ed828.aihuaPlayer.player.playqueue.PlayQueueItem

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.source.BaseMediaSource
import com.google.android.exoplayer2.source.MediaPeriod
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.Allocator

/**
 *
 * Created by Edward on 12/5/2018.
 *
 */

class PlaceholderMediaSource : BaseMediaSource(), ManagedMediaSource {
    /**
     * all those will do nothing, so this will stall the playback
     */
    override fun maybeThrowSourceInfoRefreshError() {}
    override fun createPeriod(id: MediaSource.MediaPeriodId, allocator: Allocator): MediaPeriod? = null
    override fun releasePeriod(mediaPeriod: MediaPeriod) {}
    override fun prepareSourceInternal(player: ExoPlayer, isTopLevelSource: Boolean) {}
    override fun releaseSourceInternal() {}
    override fun shouldBeReplacedWith(newIdentity: PlayQueueItem, isInterruptable: Boolean): Boolean = true
    override fun isStreamEqual(stream: PlayQueueItem): Boolean = false

}
