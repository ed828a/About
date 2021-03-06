package com.dew.ed828.aihuaPlayer.infolist.holder

import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.infolist.builder.InfoItemBuilder
import com.dew.ed828.aihuaPlayer.player.Helper.ImageDisplayConstants
import com.dew.ed828.aihuaPlayer.util.Localization
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamType

/**
 * Created by Edward on 12/13/2018.
 */

open class StreamMiniInfoItemHolder internal constructor(infoItemBuilder: InfoItemBuilder, layoutId: Int, parent: ViewGroup) : InfoItemHolder(infoItemBuilder, layoutId, parent) {

    val itemThumbnailView: ImageView
    val itemVideoTitleView: TextView
    val itemUploaderView: TextView
    val itemDurationView: TextView

    init {

        itemThumbnailView = itemView.findViewById(R.id.itemThumbnailView)
        itemVideoTitleView = itemView.findViewById(R.id.itemVideoTitleView)
        itemUploaderView = itemView.findViewById(R.id.itemUploaderView)
        itemDurationView = itemView.findViewById(R.id.itemDurationView)
    }

    constructor(infoItemBuilder: InfoItemBuilder, parent: ViewGroup) : this(infoItemBuilder, R.layout.list_stream_mini_item, parent) {}

    override fun updateFromItem(infoItem: InfoItem) {
        if (infoItem !is StreamInfoItem) return

        itemVideoTitleView.text = infoItem.name
        itemUploaderView.text = infoItem.uploaderName

        if (infoItem.duration > 0) {
            itemDurationView.text = Localization.getDurationString(infoItem.duration)
            itemDurationView.setBackgroundColor(
                ContextCompat.getColor(itemBuilder.context,
                R.color.duration_background_color))
            itemDurationView.visibility = View.VISIBLE
        } else if (infoItem.streamType == StreamType.LIVE_STREAM) {
            itemDurationView.setText(R.string.duration_live)
            itemDurationView.setBackgroundColor(ContextCompat.getColor(itemBuilder.context,
                R.color.live_duration_background_color))
            itemDurationView.visibility = View.VISIBLE
        } else {
            itemDurationView.visibility = View.GONE
        }

        // Default thumbnail is shown on error, while loading and if the url is empty
        itemBuilder.imageLoader
            .displayImage(infoItem.thumbnailUrl,
                itemThumbnailView,
                ImageDisplayConstants.DISPLAY_THUMBNAIL_OPTIONS)

        itemView.setOnClickListener { view ->
            if (itemBuilder.onStreamSelectedListener != null) {
                itemBuilder.onStreamSelectedListener!!.selected(infoItem)
            }
        }

        when (infoItem.streamType) {
            StreamType.AUDIO_STREAM, StreamType.VIDEO_STREAM, StreamType.LIVE_STREAM, StreamType.AUDIO_LIVE_STREAM -> enableLongClick(infoItem)
            StreamType.FILE, StreamType.NONE -> disableLongClick()
            else -> disableLongClick()
        }
    }

    private fun enableLongClick(item: StreamInfoItem) {
        itemView.isLongClickable = true
        itemView.setOnLongClickListener { view ->
            if (itemBuilder.onStreamSelectedListener != null) {
                itemBuilder.onStreamSelectedListener!!.held(item)
            }
            true
        }
    }

    private fun disableLongClick() {
        itemView.isLongClickable = false
        itemView.setOnLongClickListener(null)
    }
}
