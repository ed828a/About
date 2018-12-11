package com.dew.ed828.aihuaPlayer.local.holder

import android.support.v4.content.ContextCompat
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.database.LocalItem
import com.dew.ed828.aihuaPlayer.database.playlist.model.PlaylistStreamEntry
import com.dew.ed828.aihuaPlayer.local.builder.LocalItemBuilder
import com.dew.ed828.aihuaPlayer.player.Helper.ImageDisplayConstants
import com.dew.ed828.aihuaPlayer.util.Localization
import kotlinx.android.synthetic.main.list_stream_playlist_item.view.*
import org.schabi.newpipe.extractor.NewPipe
import java.text.DateFormat

/**
 *
 * Created by Edward on 12/10/2018.
 *
 */

open class LocalPlaylistStreamItemHolder internal constructor(infoItemBuilder: LocalItemBuilder,
                                                              layoutId: Int, parent: ViewGroup
) : LocalItemHolder(infoItemBuilder, layoutId, parent) {

    val itemThumbnailView: ImageView = itemView.itemThumbnailView
    val itemVideoTitleView: TextView = itemView.itemVideoTitleView
    val itemAdditionalDetailsView: TextView = itemView.itemAdditionalDetails
    val itemDurationView: TextView = itemView.itemDurationView
    val itemHandleView: View = itemView.itemHandle


    constructor(infoItemBuilder: LocalItemBuilder, parent: ViewGroup) : this(infoItemBuilder, R.layout.list_stream_playlist_item, parent)

    override fun updateFromItem(localItem: LocalItem, dateFormat: DateFormat) {
        if (localItem !is PlaylistStreamEntry) return

        itemVideoTitleView.text = localItem.title
        itemAdditionalDetailsView.text = Localization.concatenateStrings(localItem.uploader,
            NewPipe.getNameOfService(localItem.serviceId))

        if (localItem.duration > 0) {
            itemDurationView.text = Localization.getDurationString(localItem.duration)
            itemDurationView.setBackgroundColor(
                ContextCompat.getColor(itemBuilder.context!!,
                R.color.duration_background_color))
            itemDurationView.visibility = View.VISIBLE
        } else {
            itemDurationView.visibility = View.GONE
        }

        // Default thumbnail is shown on error, while loading and if the url is empty
        itemBuilder.displayImage(localItem.thumbnailUrl, itemThumbnailView,
            ImageDisplayConstants.DISPLAY_THUMBNAIL_OPTIONS)

        itemView.setOnClickListener { view ->
            if (itemBuilder.onItemSelectedListener != null) {
                itemBuilder.onItemSelectedListener!!.selected(localItem)
            }
        }

        itemView.isLongClickable = true
        itemView.setOnLongClickListener { view ->
            if (itemBuilder.onItemSelectedListener != null) {
                itemBuilder.onItemSelectedListener!!.held(localItem)
            }
            true
        }

        itemThumbnailView.setOnTouchListener(getOnTouchListener(localItem))
        itemHandleView.setOnTouchListener(getOnTouchListener(localItem))
    }

    private fun getOnTouchListener(item: PlaylistStreamEntry): View.OnTouchListener =
        View.OnTouchListener{ view, motionEvent ->
            view.performClick()
            if (itemBuilder.onItemSelectedListener != null &&
                motionEvent.actionMasked == MotionEvent.ACTION_DOWN) {
                itemBuilder.onItemSelectedListener!!.drag(item,
                    this@LocalPlaylistStreamItemHolder)
            }
            false
        }
}

