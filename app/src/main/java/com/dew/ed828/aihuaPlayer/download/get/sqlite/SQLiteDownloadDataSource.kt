package com.dew.ed828.aihuaPlayer.download.get.sqlite

import android.content.Context
import android.util.Log
import com.dew.ed828.aihuaPlayer.download.get.DownloadDataSource
import com.dew.ed828.aihuaPlayer.download.get.sqlite.DownloadMissionSQLiteHelper.Companion.KEY_LOCATION
import com.dew.ed828.aihuaPlayer.download.get.sqlite.DownloadMissionSQLiteHelper.Companion.KEY_NAME
import com.dew.ed828.aihuaPlayer.download.get.sqlite.DownloadMissionSQLiteHelper.Companion.MISSIONS_TABLE_NAME
import com.dew.ed828.aihuaPlayer.download.model.DownloadMission

/**
 *
 * Created by Edward on 12/11/2018.
 *
 * Non-thread-safe implementation of [DownloadDataSource]
 */
class SQLiteDownloadDataSource(context: Context) : DownloadDataSource {
    private val downloadMissionSQLiteHelper: DownloadMissionSQLiteHelper = DownloadMissionSQLiteHelper(context)

    override fun loadMissions(): List<DownloadMission> {
        val result: ArrayList<DownloadMission>
        val database = downloadMissionSQLiteHelper.readableDatabase
        val cursor = database.query(MISSIONS_TABLE_NAME, null, null, null, null, null, /* sorted by */ DownloadMissionSQLiteHelper.KEY_TIMESTAMP)

        val count = cursor.count
        if (count == 0) return ArrayList()
        result = ArrayList(count)
        while (cursor.moveToNext()) {
            result.add(DownloadMissionSQLiteHelper.getMissionFromCursor(cursor))
        }
        return result
    }

    override fun addMission(downloadMission: DownloadMission) {
        val database = downloadMissionSQLiteHelper.writableDatabase
        val values = DownloadMissionSQLiteHelper.getValuesOfMission(downloadMission)
        database.insert(MISSIONS_TABLE_NAME, null, values)
    }

    override fun updateMission(downloadMission: DownloadMission) {
        val database = downloadMissionSQLiteHelper.writableDatabase
        val values = DownloadMissionSQLiteHelper.getValuesOfMission(downloadMission)
        val whereClause = "$KEY_LOCATION = ? AND $KEY_NAME = ?"
        val rowsAffected = database.update(MISSIONS_TABLE_NAME, values,
            whereClause, arrayOf(downloadMission.location, downloadMission.name))
        if (rowsAffected != 1) {
            Log.e(TAG, "Error: Expected 1 row to be affected by update but got $rowsAffected rows affected")
        }
    }

    override fun deleteMission(downloadMission: DownloadMission) {
        val database = downloadMissionSQLiteHelper.writableDatabase
        database.delete(MISSIONS_TABLE_NAME,
            "$KEY_LOCATION = ? AND $KEY_NAME = ?",
            arrayOf(downloadMission.location, downloadMission.name))
    }

    companion object {
        private const val TAG = "DownloadDataSourceImpl"
    }
}
