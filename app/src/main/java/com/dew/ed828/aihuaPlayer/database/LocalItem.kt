package com.dew.ed828.aihuaPlayer.database

/**
 *
 * Created by Edward on 12/4/2018.
 *
 */

interface LocalItem {
    val localItemType: LocalItemType

    enum class LocalItemType {
        PLAYLIST_LOCAL_ITEM,
        PLAYLIST_REMOTE_ITEM,

        PLAYLIST_STREAM_ITEM,
        STATISTIC_STREAM_ITEM
    }
}