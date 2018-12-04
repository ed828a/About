package com.dew.ed828.aihuaPlayer.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import com.dew.ed828.aihuaPlayer.database.Migrations.DB_VER_12_0
import com.dew.ed828.aihuaPlayer.database.history.dao.SearchHistoryDAO
import com.dew.ed828.aihuaPlayer.database.history.dao.StreamHistoryDAO
import com.dew.ed828.aihuaPlayer.database.history.model.SearchHistoryEntry
import com.dew.ed828.aihuaPlayer.database.history.model.StreamHistoryEntity
import com.dew.ed828.aihuaPlayer.database.playlist.dao.PlaylistDAO
import com.dew.ed828.aihuaPlayer.database.playlist.dao.PlaylistRemoteDAO
import com.dew.ed828.aihuaPlayer.database.playlist.dao.PlaylistStreamDAO
import com.dew.ed828.aihuaPlayer.database.playlist.model.PlaylistEntity
import com.dew.ed828.aihuaPlayer.database.playlist.model.PlaylistRemoteEntity
import com.dew.ed828.aihuaPlayer.database.playlist.model.PlaylistStreamEntity
import com.dew.ed828.aihuaPlayer.database.stream.dao.StreamDAO
import com.dew.ed828.aihuaPlayer.database.stream.dao.StreamStateDAO
import com.dew.ed828.aihuaPlayer.database.stream.model.StreamEntity
import com.dew.ed828.aihuaPlayer.database.stream.model.StreamStateEntity
import com.dew.ed828.aihuaPlayer.database.subscription.SubscriptionDAO
import com.dew.ed828.aihuaPlayer.database.subscription.SubscriptionEntity

/**
 *
 * Created by Edward on 12/4/2018.
 *
 */

@TypeConverters(Converters::class)
@Database(entities = [
    SubscriptionEntity::class,
    SearchHistoryEntry::class,
    StreamEntity::class,
    StreamHistoryEntity::class,
    StreamStateEntity::class,
    PlaylistEntity::class,
    PlaylistStreamEntity::class,
    PlaylistRemoteEntity::class
],
    version = DB_VER_12_0, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subscriptionDAO(): SubscriptionDAO

    abstract fun searchHistoryDAO(): SearchHistoryDAO

    abstract fun streamDAO(): StreamDAO

    abstract fun streamHistoryDAO(): StreamHistoryDAO

    abstract fun streamStateDAO(): StreamStateDAO

    abstract fun playlistDAO(): PlaylistDAO

    abstract fun playlistStreamDAO(): PlaylistStreamDAO

    abstract fun playlistRemoteDAO(): PlaylistRemoteDAO

    companion object {

        const val DATABASE_NAME = "edplayer.db"
    }
}
