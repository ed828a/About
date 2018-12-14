package com.dew.ed828.aihuaPlayer.fragments.common

/**
 * Created by Edward on 12/13/2018.
 */

interface ListViewContract<I, N> : ViewContract<I> {
    fun showListFooter(show: Boolean)

    fun handleNextItems(result: N)
}
