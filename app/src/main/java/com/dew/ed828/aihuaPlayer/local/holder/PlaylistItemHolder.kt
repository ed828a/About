package com.dew.ed828.aihuaPlayer.local.holder

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.about.R.string.view
import com.dew.ed828.aihuaPlayer.database.LocalItem
import com.dew.ed828.aihuaPlayer.local.builder.LocalItemBuilder
import kotlinx.android.synthetic.main.list_playlist_mini_item.view.*
import java.text.DateFormat

/**
 *
 * Created by Edward on 12/10/2018.
 *
 */

abstract class PlaylistItemHolder(
    infoItemBuilder: LocalItemBuilder,
    layoutId: Int,
    parent: ViewGroup
) : LocalItemHolder(infoItemBuilder, layoutId, parent) {

    val itemThumbnailView: ImageView = itemView.itemThumbnailView
    val itemStreamCountView: TextView = itemView.itemStreamCountView
    val itemTitleView: TextView = itemView.itemTitleView
    val itemUploaderView: TextView = itemView.itemUploaderView

    constructor(infoItemBuilder: LocalItemBuilder, parent: ViewGroup) : this(
        infoItemBuilder,
        R.layout.list_playlist_mini_item,
        parent
    )


    override fun updateFromItem(localItem: LocalItem, dateFormat: DateFormat) {
        itemView.setOnClickListener { view ->
            itemBuilder.onItemSelectedListener?.selected(localItem)
        }

        itemView.isLongClickable = true

        itemView.setOnLongClickListener { view ->
            itemBuilder.onItemSelectedListener?.held(localItem)
            true
        }

    }
}

