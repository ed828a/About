package com.dew.ed828.aihuaPlayer.infolist.holder

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.dew.ed828.aihuaPlayer.infolist.builder.InfoItemBuilder
import org.schabi.newpipe.extractor.InfoItem

/**
 * Created by Edward on 12/13/2018.
 */

abstract class InfoItemHolder(
    protected val itemBuilder: InfoItemBuilder,
    layoutId: Int,
    parent: ViewGroup
) : RecyclerView.ViewHolder(LayoutInflater.from(itemBuilder.context).inflate(layoutId, parent, false)) {

    abstract fun updateFromItem(infoItem: InfoItem)
}
