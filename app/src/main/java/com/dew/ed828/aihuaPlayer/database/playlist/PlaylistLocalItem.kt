package com.dew.ed828.aihuaPlayer.database.playlist

import com.dew.ed828.aihuaPlayer.database.LocalItem

/**
 *
 * Created by Edward on 12/4/2018.
 *
 */

interface PlaylistLocalItem : LocalItem {
    fun getOrderingName(): String?
}