package com.dew.ed828.aihuaPlayer.player.mediasession

import android.support.v4.media.MediaDescriptionCompat

/**
 *
 * Created by Edward on 12/6/2018.
 *
 */

interface MediaSessionCallback {
    fun getCurrentPlayingIndex(): Int
    fun getQueueSize(): Int

    fun onSkipToPrevious()
    fun onSkipToNext()
    fun onSkipToIndex(index: Int)

    fun getQueueMetadata(index: Int): MediaDescriptionCompat?

    fun onPlay()
    fun onPause()
}