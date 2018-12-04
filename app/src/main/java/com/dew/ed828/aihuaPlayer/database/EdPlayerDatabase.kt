package com.dew.ed828.aihuaPlayer.database

import android.arch.persistence.room.Room
import android.content.Context
import com.dew.ed828.aihuaPlayer.database.AppDatabase.Companion.DATABASE_NAME
import com.dew.ed828.aihuaPlayer.database.Migrations.MIGRATION_11_12

/**
 *
 * Created by Edward on 12/4/2018.
 *
 */

object EdPlayerDatabase {
    @Volatile
    private var databaseInstance: AppDatabase? = null

    private fun getDatabase(context: Context): AppDatabase {
        return Room
            .databaseBuilder(context.applicationContext, AppDatabase::class.java, DATABASE_NAME)
            .addMigrations(MIGRATION_11_12)
            .fallbackToDestructiveMigration()
            .build()
    }

    fun getInstance(context: Context): AppDatabase {
        var result = databaseInstance
        if (result == null) {
            synchronized(EdPlayerDatabase::class.java) {
                result = databaseInstance
                if (result == null) {
                    result = getDatabase(context)
                    databaseInstance = result
                }
            }
        }

        return result!!
    }
}