package com.dew.ed828.aihuaPlayer.download.get

import com.dew.ed828.aihuaPlayer.download.model.DownloadMission

/**
 *
 * Created by Edward on 12/11/2018.
 *
 * Provides access to the storage of [DownloadMission]s
 * when convert this to Room, this file should be DAO
 */
interface DownloadDataSource {

    /**
     * Load all missions
     *
     * @return a list of download missions
     */
    fun loadMissions(): List<DownloadMission>

    /**
     * Add a download mission to the storage
     *
     * @param downloadMission the download mission to add
     * @return the identifier of the mission
     */
    fun addMission(downloadMission: DownloadMission)

    /**
     * Update a download mission which exists in the storage
     *
     * @param downloadMission the download mission to update
     * @throws IllegalArgumentException if the mission was not added to storage
     */
    fun updateMission(downloadMission: DownloadMission)


    /**
     * Delete a download mission
     *
     * @param downloadMission the mission to delete
     */
    fun deleteMission(downloadMission: DownloadMission)
}