package com.dew.ed828.aihuaPlayer.local.holder

import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.database.LocalItem
import com.dew.ed828.aihuaPlayer.database.stream.model.StreamStatisticsEntry
import com.dew.ed828.aihuaPlayer.local.builder.LocalItemBuilder
import com.dew.ed828.aihuaPlayer.player.Helper.ImageDisplayConstants
import com.dew.ed828.aihuaPlayer.util.Localization
import kotlinx.android.synthetic.main.list_statistics_stream_item.view.*
import org.schabi.newpipe.extractor.NewPipe
import java.text.DateFormat

/**
 *
 * Created by Edward on 12/10/2018.
 *
 */

open class LocalStatisticStreamItemHolder internal constructor(infoItemBuilder: LocalItemBuilder, layoutId: Int, parent: ViewGroup) : LocalItemHolder(infoItemBuilder, layoutId, parent) {

    val itemThumbnailView: ImageView
    val itemVideoTitleView: TextView
    val itemUploaderView: TextView
    val itemDurationView: TextView
    val itemAdditionalDetails: TextView?

    constructor(itemBuilder: LocalItemBuilder, parent: ViewGroup) : this(itemBuilder, R.layout.list_statistics_stream_item, parent) {}

    init {

        itemThumbnailView = itemView.itemThumbnailView
        itemVideoTitleView = itemView.itemVideoTitleView
        itemUploaderView = itemView.itemUploaderView
        itemDurationView = itemView.itemDurationView
        itemAdditionalDetails = itemView.itemAdditionalDetails
    }

    private fun getStreamInfoDetailLine(entry: StreamStatisticsEntry,
                                        dateFormat: DateFormat): String {
        val watchCount = Localization.shortViewCount(itemBuilder.context!!,
            entry.watchCount)
        val uploadDate = dateFormat.format(entry.latestAccessDate)
        val serviceName = NewPipe.getNameOfService(entry.serviceId)
        return Localization.concatenateStrings(watchCount, uploadDate, serviceName)
    }

    override fun updateFromItem(localItem: LocalItem, dateFormat: DateFormat) {
        if (localItem !is StreamStatisticsEntry) return

        itemVideoTitleView.text = localItem.title
        itemUploaderView.text = localItem.uploader

        if (localItem.duration > 0) {
            itemDurationView.text = Localization.getDurationString(localItem.duration)
            itemDurationView.setBackgroundColor(
                ContextCompat.getColor(itemBuilder.context!!,
                R.color.duration_background_color))
            itemDurationView.visibility = View.VISIBLE
        } else {
            itemDurationView.visibility = View.GONE
        }

        if (itemAdditionalDetails != null) {
            itemAdditionalDetails.text = getStreamInfoDetailLine(localItem, dateFormat)
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
    }
}
