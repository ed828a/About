package com.dew.ed828.aihuaPlayer.player.resolver

import android.content.Context
import com.dew.ed828.aihuaPlayer.player.Helper.PlayerDataSource
import com.dew.ed828.aihuaPlayer.player.Helper.PlayerHelper
import com.dew.ed828.aihuaPlayer.util.ListHelper
import com.google.android.exoplayer2.source.MediaSource
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.stream.StreamInfo

/**
 *
 * Created by Edward on 12/7/2018.
 *
 */

class AudioPlaybackResolver(private val context: Context,
                            private val dataSource: PlayerDataSource
) : PlaybackResolver {

    override fun resolve(info: StreamInfo): MediaSource? {
        val liveSource = maybeBuildLiveMediaSource(dataSource, info)
        if (liveSource != null) return liveSource

        val index = ListHelper.getDefaultAudioFormat(context, info.audioStreams)
        if (index < 0 || index >= info.audioStreams.size) return null

        val audio = info.audioStreams[index]
        val tag = MediaSourceTag(info)

        return buildMediaSource(dataSource, audio.getUrl(), PlayerHelper.cacheKeyOf(info, audio), MediaFormat.getSuffixById(audio.formatId), tag)
    }
}
