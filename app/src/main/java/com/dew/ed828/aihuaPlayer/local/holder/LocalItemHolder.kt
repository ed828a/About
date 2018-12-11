package com.dew.ed828.aihuaPlayer.local.holder

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.dew.ed828.aihuaPlayer.database.LocalItem
import com.dew.ed828.aihuaPlayer.local.builder.LocalItemBuilder
import java.text.DateFormat

/**
 *
 * Created by Edward on 12/10/2018.
 *
 */

abstract class LocalItemHolder(protected val itemBuilder: LocalItemBuilder, layoutId: Int, parent: ViewGroup)
    : RecyclerView.ViewHolder(LayoutInflater.from(itemBuilder.context).inflate(layoutId, parent, false)) {

    abstract fun updateFromItem(item: LocalItem, dateFormat: DateFormat)
}
