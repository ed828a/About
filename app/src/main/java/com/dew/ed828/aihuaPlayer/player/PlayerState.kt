package com.dew.ed828.aihuaPlayer.player

import com.dew.ed828.aihuaPlayer.player.playqueue.PlayQueue
import java.io.Serializable

/**
 *
 * Created by Edward on 12/7/2018.
 *
 * Todo: need to move to model package
 */

class PlayerState(
    val playQueue: PlayQueue,
    val repeatMode: Int,
    val playbackSpeed: Float,
    val playbackPitch: Float,
    val playbackQuality: String?,
    val isPlaybackSkipSilence: Boolean,
    private val wasPlaying: Boolean) : Serializable {

    internal constructor(playQueue: PlayQueue,
                         repeatMode: Int,
                         playbackSpeed: Float,
                         playbackPitch: Float,
                         playbackSkipSilence:
                         Boolean, wasPlaying: Boolean) : this(playQueue, repeatMode, playbackSpeed, playbackPitch, null,
        playbackSkipSilence, wasPlaying) {
    }

    fun wasPlaying(): Boolean {
        return wasPlaying
    }
}