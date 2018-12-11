package com.dew.ed828.aihuaPlayer.player

import com.google.android.exoplayer2.PlaybackParameters
import org.schabi.newpipe.extractor.stream.StreamInfo

/**
 *
 * Created by Edward on 12/7/2018.
 *
 */

interface PlayerEventListener {
    fun onPlaybackUpdate(state: Int, repeatMode: Int, shuffled: Boolean, parameters: PlaybackParameters)
    fun onProgressUpdate(currentProgress: Int, duration: Int, bufferPercent: Int)
    fun onMetadataUpdate(info: StreamInfo?)
    fun onServiceStopped()
}
