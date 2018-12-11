package com.dew.ed828.aihuaPlayer.player.playqueue

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.play_queue_item.view.*

/**
 *
 * Created by Edward on 12/5/2018.
 *
 */

class PlayQueueItemHolder(view: View): RecyclerView.ViewHolder(view) {
    val itemRoot: View = view.playQueueItemRoot

    val itemVideoTitleView: TextView = view.playQueueItemVideoTitleView
    val itemDurationView: TextView = view.playQueueItemDurationView
    val itemAdditionalDetailsView: TextView = view.playQueueItemAdditionalDetails
    val itemSelected: ImageView = view.playQueueItemSelected
    val itemThumbnailView: ImageView = view.playQueueItemThumbnailView
    val itemHandle: ImageView = view.playQueueItemHandle
}