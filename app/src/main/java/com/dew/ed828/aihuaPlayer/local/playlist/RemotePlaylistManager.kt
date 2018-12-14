package com.dew.ed828.aihuaPlayer.local.playlist

import com.dew.ed828.aihuaPlayer.database.AppDatabase
import com.dew.ed828.aihuaPlayer.database.playlist.dao.PlaylistRemoteDAO
import com.dew.ed828.aihuaPlayer.database.playlist.model.PlaylistRemoteEntity
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.schabi.newpipe.extractor.playlist.PlaylistInfo

/**
 * Created by Edward on 12/13/2018.
 */

class RemotePlaylistManager(db: AppDatabase) {

    private val playlistRemoteTable: PlaylistRemoteDAO = db.playlistRemoteDAO()

    val playlists: Flowable<List<PlaylistRemoteEntity>>
        get() = playlistRemoteTable.all.subscribeOn(Schedulers.io())

    fun getPlaylist(info: PlaylistInfo): Flowable<List<PlaylistRemoteEntity>> {
        return playlistRemoteTable.getPlaylist(info.serviceId.toLong(), info.url)
            .subscribeOn(Schedulers.io())
    }

    fun deletePlaylist(playlistId: Long): Single<Int> {
        return Single.fromCallable { playlistRemoteTable.deletePlaylist(playlistId) }
            .subscribeOn(Schedulers.io())
    }

    fun onBookmark(playlistInfo: PlaylistInfo): Single<Long> {
        return Single.fromCallable {
            val playlist = PlaylistRemoteEntity(playlistInfo)
            playlistRemoteTable.upsert(playlist)
        }.subscribeOn(Schedulers.io())
    }

    fun onUpdate(playlistId: Long, playlistInfo: PlaylistInfo): Single<Int> {
        return Single.fromCallable {
            val playlist = PlaylistRemoteEntity(playlistInfo)
            playlist.uid = playlistId
            playlistRemoteTable.update(playlist)
        }.subscribeOn(Schedulers.io())
    }
}
