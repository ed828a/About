package com.dew.ed828.aihuaPlayer.util

import android.support.v7.widget.RecyclerView

/**
 *
 * Created by Edward on 12/10/2018.
 *
 */
abstract class OnClickGesture<T> {

    abstract fun selected(selectedItem: T)

    open fun held(selectedItem: T) {
        // Optional gesture
    }

    open fun drag(selectedItem: T, viewHolder: RecyclerView.ViewHolder) {
        // Optional gesture
    }
}
