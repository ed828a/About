package com.dew.ed828.aihuaPlayer.database.playlist.model

import android.arch.persistence.room.*
import com.dew.ed828.aihuaPlayer.database.LocalItem
import com.dew.ed828.aihuaPlayer.database.playlist.PlaylistLocalItem
import com.dew.ed828.aihuaPlayer.database.playlist.model.PlaylistRemoteEntity.Companion.REMOTE_PLAYLIST_NAME
import com.dew.ed828.aihuaPlayer.database.playlist.model.PlaylistRemoteEntity.Companion.REMOTE_PLAYLIST_SERVICE_ID
import com.dew.ed828.aihuaPlayer.database.playlist.model.PlaylistRemoteEntity.Companion.REMOTE_PLAYLIST_TABLE
import com.dew.ed828.aihuaPlayer.database.playlist.model.PlaylistRemoteEntity.Companion.REMOTE_PLAYLIST_URL
import com.dew.ed828.aihuaPlayer.util.NO_SERVICE_ID
import org.schabi.newpipe.extractor.playlist.PlaylistInfo

/**
 *
 * Created by Edward on 12/4/2018.
 *
 */

@Entity(
    tableName = REMOTE_PLAYLIST_TABLE,
    indices = [
        Index(value = arrayOf(REMOTE_PLAYLIST_NAME)),
        Index(
            value = arrayOf(REMOTE_PLAYLIST_SERVICE_ID, REMOTE_PLAYLIST_URL),
            unique = true
        )]
)
class PlaylistRemoteEntity(
    @field:ColumnInfo(name = REMOTE_PLAYLIST_SERVICE_ID)
    var serviceId: Int = NO_SERVICE_ID,

    @field:ColumnInfo(name = REMOTE_PLAYLIST_NAME)
    var name: String?,

    @field:ColumnInfo(name = REMOTE_PLAYLIST_URL)
    var url: String?,

    @field:ColumnInfo(name = REMOTE_PLAYLIST_THUMBNAIL_URL)
    var thumbnailUrl: String?,

    @field:ColumnInfo(name = REMOTE_PLAYLIST_UPLOADER_NAME)
    var uploader: String?,

    @field:ColumnInfo(name = REMOTE_PLAYLIST_STREAM_COUNT)
    var streamCount: Long?
) : PlaylistLocalItem {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = REMOTE_PLAYLIST_ID)
    var uid: Long = 0

    override val localItemType: LocalItem.LocalItemType
        get() = LocalItem.LocalItemType.PLAYLIST_REMOTE_ITEM

    @Ignore
    constructor(info: PlaylistInfo) : this(
        info.serviceId, info.name, info.url,
        if (info.thumbnailUrl == null) info.uploaderAvatarUrl else info.thumbnailUrl,
        info.uploaderName, info.streamCount
    ) {
    }

    @Ignore
    fun isIdenticalTo(info: PlaylistInfo): Boolean {
        return serviceId == info.serviceId && name == info.name &&
                streamCount == info.streamCount && url == info.url &&
                thumbnailUrl == info.thumbnailUrl &&
                uploader == info.uploaderName
    }

    override fun getOrderingName(): String? {
        return name
    }

    companion object {
        const val REMOTE_PLAYLIST_TABLE = "remote_playlists"
        const val REMOTE_PLAYLIST_ID = "uid"
        const val REMOTE_PLAYLIST_SERVICE_ID = "service_id"
        const val REMOTE_PLAYLIST_NAME = "name"
        const val REMOTE_PLAYLIST_URL = "url"
        const val REMOTE_PLAYLIST_THUMBNAIL_URL = "thumbnail_url"
        const val REMOTE_PLAYLIST_UPLOADER_NAME = "uploader"
        const val REMOTE_PLAYLIST_STREAM_COUNT = "stream_count"
    }
}
