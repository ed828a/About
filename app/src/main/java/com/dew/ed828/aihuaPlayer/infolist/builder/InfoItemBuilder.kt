package com.dew.ed828.aihuaPlayer.infolist.builder

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.dew.ed828.aihuaPlayer.infolist.holder.*
import com.dew.ed828.aihuaPlayer.util.OnClickGesture
import com.nostra13.universalimageloader.core.ImageLoader
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem

/**
 * Created by Edward on 12/13/2018.
 */

class InfoItemBuilder(val context: Context) {
    val imageLoader = ImageLoader.getInstance()

    var onStreamSelectedListener: OnClickGesture<StreamInfoItem>? = null
    var onChannelSelectedListener: OnClickGesture<ChannelInfoItem>? = null
    var onPlaylistSelectedListener: OnClickGesture<PlaylistInfoItem>? = null

    fun buildView(parent: ViewGroup, infoItem: InfoItem, useMiniVariant: Boolean = false): View {
        val holder = holderFromInfoType(parent, infoItem.infoType, useMiniVariant)
        holder.updateFromItem(infoItem)
        return holder.itemView
    }

    private fun holderFromInfoType(parent: ViewGroup, infoType: InfoItem.InfoType, useMiniVariant: Boolean): InfoItemHolder {
        return when (infoType) {
            InfoItem.InfoType.STREAM -> if (useMiniVariant) StreamMiniInfoItemHolder(this, parent) else StreamInfoItemHolder(this, parent)
            InfoItem.InfoType.CHANNEL -> if (useMiniVariant) ChannelMiniInfoItemHolder(this, parent) else ChannelInfoItemHolder(this, parent)
            InfoItem.InfoType.PLAYLIST -> if (useMiniVariant) PlaylistMiniInfoItemHolder(this, parent) else PlaylistInfoItemHolder(this, parent)
            else -> {
                Log.e(TAG, "InfoType not expected = ${infoType.name}")
                throw RuntimeException("InfoType not expected = ${infoType.name}")
            }
        }
    }

    companion object {
        private val TAG = InfoItemBuilder::class.java.toString()
    }

}
