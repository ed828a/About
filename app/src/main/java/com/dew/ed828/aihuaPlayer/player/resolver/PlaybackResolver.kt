package com.dew.ed828.aihuaPlayer.player.resolver

import android.annotation.SuppressLint
import android.net.Uri
import android.text.TextUtils
import com.dew.ed828.aihuaPlayer.player.Helper.PlayerDataSource
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.util.Util
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamType

/**
 *
 * Created by Edward on 12/6/2018.
 *
 */

interface PlaybackResolver : Resolver<StreamInfo, MediaSource> {

    fun maybeBuildLiveMediaSource(dataSource: PlayerDataSource, info: StreamInfo): MediaSource? {

        val streamType = info.streamType

        if (streamType != StreamType.AUDIO_LIVE_STREAM && streamType != StreamType.LIVE_STREAM) {
            return null
        }

        val tag = MediaSourceTag(info)
        return when {
            info.hlsUrl.isNotEmpty() -> buildLiveMediaSource(dataSource, info.hlsUrl, C.TYPE_HLS, tag)
            info.dashMpdUrl.isNotEmpty() -> buildLiveMediaSource(dataSource, info.dashMpdUrl, C.TYPE_DASH, tag)
            else -> null
        }
    }

    @SuppressLint("SwitchIntDef")
    fun buildLiveMediaSource(dataSource: PlayerDataSource,
                             sourceUrl: String,
                             @C.ContentType type: Int,
                             metadata: MediaSourceTag): MediaSource {

        val uri = Uri.parse(sourceUrl)
        return when (type) { // SS means SmoothStream
            C.TYPE_SS -> dataSource.liveSsMediaSourceFactory.setTag(metadata).createMediaSource(uri)

            C.TYPE_DASH -> dataSource.liveDashMediaSourceFactory.setTag(metadata).createMediaSource(uri)

            C.TYPE_HLS -> dataSource.liveHlsMediaSourceFactory.setTag(metadata).createMediaSource(uri)

            else -> throw IllegalStateException("Unsupported type: $type")
        }
    }

    fun buildMediaSource(dataSource: PlayerDataSource,
                         sourceUrl: String,
                         cacheKey: String,
                         overrideExtension: String,
                         metadata: MediaSourceTag): MediaSource {

        val uri = Uri.parse(sourceUrl)
        @C.ContentType val type = if (TextUtils.isEmpty(overrideExtension))
            Util.inferContentType(uri)
        else
            Util.inferContentType(".$overrideExtension")

        return when (type) {
            C.TYPE_SS -> dataSource.liveSsMediaSourceFactory.setTag(metadata).createMediaSource(uri)

            C.TYPE_DASH -> dataSource.dashMediaSourceFactory.setTag(metadata).createMediaSource(uri)

            C.TYPE_HLS -> dataSource.hlsMediaSourceFactory.setTag(metadata).createMediaSource(uri)

            C.TYPE_OTHER -> dataSource.getExtractorMediaSourceFactory(cacheKey).setTag(metadata).createMediaSource(uri)

            else -> throw IllegalStateException("Unsupported type: $type")
        }
    }
}
