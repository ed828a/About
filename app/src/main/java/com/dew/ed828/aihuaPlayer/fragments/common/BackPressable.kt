package com.dew.ed828.aihuaPlayer.fragments.common

/**
 * Created by Edward on 12/13/2018.
 */

interface BackPressable {
    /**
     * A back press was delegated to this fragment
     *
     * @return if the back press was handled
     */
    fun onBackPressed(): Boolean
}