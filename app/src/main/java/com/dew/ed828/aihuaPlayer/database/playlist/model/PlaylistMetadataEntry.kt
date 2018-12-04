package com.dew.ed828.aihuaPlayer.database.playlist.model

import android.arch.persistence.room.ColumnInfo
import com.dew.ed828.aihuaPlayer.database.LocalItem
import com.dew.ed828.aihuaPlayer.database.playlist.PlaylistLocalItem
import com.dew.ed828.aihuaPlayer.database.playlist.model.PlaylistEntity.Companion.PLAYLIST_ID
import com.dew.ed828.aihuaPlayer.database.playlist.model.PlaylistEntity.Companion.PLAYLIST_NAME
import com.dew.ed828.aihuaPlayer.database.playlist.model.PlaylistEntity.Companion.PLAYLIST_THUMBNAIL_URL

/**
 *
 * Created by Edward on 12/4/2018.
 *
 */
class PlaylistMetadataEntry(
    @field:ColumnInfo(name = PLAYLIST_ID) val uid: Long,
    @field:ColumnInfo(name = PLAYLIST_NAME) val name: String,
    @field:ColumnInfo(name = PLAYLIST_THUMBNAIL_URL) val thumbnailUrl: String,
    @field:ColumnInfo(name = PLAYLIST_STREAM_COUNT) val streamCount: Long
) : PlaylistLocalItem {

    override val localItemType: LocalItem.LocalItemType
        get() = LocalItem.LocalItemType.PLAYLIST_LOCAL_ITEM

    override fun getOrderingName(): String = name

    companion object {
        const val PLAYLIST_STREAM_COUNT = "streamCount"
    }
}