package com.dew.ed828.aihuaPlayer.player.Helper

import android.content.Context
import android.content.Intent
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.view.KeyEvent
import com.dew.ed828.aihuaPlayer.player.mediasession.MediaSessionCallback
import com.dew.ed828.aihuaPlayer.player.mediasession.PlayQueueNavigator
import com.dew.ed828.aihuaPlayer.player.mediasession.PlayQueuePlaybackController
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector

/**
 *
 * Created by Edward on 12/6/2018.
 *
 */

class MediaSessionManager(context: Context,
                          player: Player,
                          callback: MediaSessionCallback
) {

    private val mediaSession: MediaSessionCompat
    private val sessionConnector: MediaSessionConnector

    init {
        this.mediaSession = MediaSessionCompat(context, TAG)
        this.mediaSession.isActive = true

        this.sessionConnector = MediaSessionConnector(mediaSession,
            PlayQueuePlaybackController(callback)
        )
        this.sessionConnector.setQueueNavigator(PlayQueueNavigator(mediaSession, callback))
        this.sessionConnector.setPlayer(player, null)
    }

    fun handleMediaButtonIntent(intent: Intent): KeyEvent? {
        return MediaButtonReceiver.handleIntent(mediaSession, intent)
    }

    /**
     * Should be called on player destruction to prevent leakage.
     */
    fun dispose() {
        this.sessionConnector.setPlayer(null, null)
        this.sessionConnector.setQueueNavigator(null)
        this.mediaSession.isActive = false
        this.mediaSession.release()
    }

    companion object {
        private const val TAG = "MediaSessionManager"
    }
}
