package com.dew.ed828.aihuaPlayer.infolist.holder

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.infolist.builder.InfoItemBuilder
import com.dew.ed828.aihuaPlayer.player.Helper.ImageDisplayConstants
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem

/**
 * Created by Edward on 12/13/2018.
 */

open class PlaylistMiniInfoItemHolder(infoItemBuilder: InfoItemBuilder, layoutId: Int, parent: ViewGroup) : InfoItemHolder(infoItemBuilder, layoutId, parent) {
    val itemThumbnailView: ImageView = itemView.findViewById(R.id.itemThumbnailView)
    val itemStreamCountView: TextView = itemView.findViewById(R.id.itemStreamCountView)
    val itemTitleView: TextView = itemView.findViewById(R.id.itemTitleView)
    val itemUploaderView: TextView = itemView.findViewById(R.id.itemUploaderView)

    constructor(infoItemBuilder: InfoItemBuilder, parent: ViewGroup) : this(infoItemBuilder, R.layout.list_playlist_mini_item, parent) {}

    override fun updateFromItem(infoItem: InfoItem) {
        if (infoItem !is PlaylistInfoItem) return

        itemTitleView.text = infoItem.name
        itemStreamCountView.text = infoItem.streamCount.toString()
        itemUploaderView.text = infoItem.uploaderName

        itemBuilder.imageLoader
            .displayImage(infoItem.thumbnailUrl, itemThumbnailView,
                ImageDisplayConstants.DISPLAY_THUMBNAIL_OPTIONS)

        itemView.setOnClickListener { view ->
            if (itemBuilder.onPlaylistSelectedListener != null) {
                itemBuilder.onPlaylistSelectedListener!!.selected(infoItem)
            }
        }

        itemView.isLongClickable = true
        itemView.setOnLongClickListener { view ->
            if (itemBuilder.onPlaylistSelectedListener != null) {
                itemBuilder.onPlaylistSelectedListener!!.held(infoItem)
            }
            true
        }
    }
}
