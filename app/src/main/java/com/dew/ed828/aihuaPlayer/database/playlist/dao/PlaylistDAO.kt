package com.dew.ed828.aihuaPlayer.database.playlist.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.dew.ed828.aihuaPlayer.database.BasicDAO
import com.dew.ed828.aihuaPlayer.database.playlist.model.PlaylistEntity
import com.dew.ed828.aihuaPlayer.database.playlist.model.PlaylistEntity.Companion.PLAYLIST_ID
import com.dew.ed828.aihuaPlayer.database.playlist.model.PlaylistEntity.Companion.PLAYLIST_TABLE
import io.reactivex.Flowable

/**
 *
 * Created by Edward on 12/4/2018.
 *
 */

@Dao
abstract class PlaylistDAO : BasicDAO<PlaylistEntity> {
    @get:Query("SELECT * FROM $PLAYLIST_TABLE")
    abstract override val all: Flowable<List<PlaylistEntity>>

    @Query("DELETE FROM $PLAYLIST_TABLE")
    abstract override fun deleteAll(): Int

    override fun listByService(serviceId: Int): Flowable<List<PlaylistEntity>> {
        throw UnsupportedOperationException()
    }

    @Query("SELECT * FROM $PLAYLIST_TABLE WHERE $PLAYLIST_ID = :playlistId")
    abstract fun getPlaylist(playlistId: Long): Flowable<List<PlaylistEntity>>

    @Query("DELETE FROM $PLAYLIST_TABLE WHERE $PLAYLIST_ID = :playlistId")
    abstract fun deletePlaylist(playlistId: Long): Int
}
