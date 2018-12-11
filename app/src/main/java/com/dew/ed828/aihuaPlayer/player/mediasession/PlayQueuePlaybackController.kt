package com.dew.ed828.aihuaPlayer.player.mediasession

import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.DefaultPlaybackController

/**
 *
 * Created by Edward on 12/6/2018.
 *
 */

class PlayQueuePlaybackController(private val callback: MediaSessionCallback) : DefaultPlaybackController() {

    override fun onPlay(player: Player) {
        callback.onPlay()
    }

    override fun onPause(player: Player) {
        callback.onPause()
    }
}
