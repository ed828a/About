package com.dew.ed828.aihuaPlayer.player.resolver

import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.VideoStream
import java.io.Serializable

/**
 *
 * Created by Edward on 12/6/2018.
 *
 */

class MediaSourceTag @JvmOverloads constructor(val metadata: StreamInfo,
                                               val sortedAvailableVideoStreams: List<VideoStream> = emptyList(),
                                               val selectedVideoStreamIndex: Int = -1/*indexNotAvailable=*/) : Serializable {

    val selectedVideoStream: VideoStream?
        get() =
            if (selectedVideoStreamIndex < 0 || selectedVideoStreamIndex >= sortedAvailableVideoStreams.size)
                null
            else
                sortedAvailableVideoStreams[selectedVideoStreamIndex]
}
