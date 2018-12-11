package com.dew.ed828.aihuaPlayer.player.mediasource

import com.dew.ed828.aihuaPlayer.player.playqueue.PlayQueueItem
import com.google.android.exoplayer2.source.MediaSource

/**
 *
 * Created by Edward on 12/5/2018.
 *
 */

interface ManagedMediaSource : MediaSource {
    /**
     * Determines whether or not this [ManagedMediaSource] can be replaced.
     *
     * @param newIdentity: a stream the [ManagedMediaSource] should encapsulate over, if
     * it is different from the existing stream in the [ManagedMediaSource],
     * then it should be replaced.
     * @param isInterruptable: specifies if this [ManagedMediaSource] potentially
     * being played.
     */
    fun shouldBeReplacedWith(newIdentity: PlayQueueItem,
                             isInterruptable: Boolean): Boolean

    /**
     * Determines if the [PlayQueueItem] is the one the
     * [ManagedMediaSource] encapsulates over.
     */
    fun isStreamEqual(stream: PlayQueueItem): Boolean
}
