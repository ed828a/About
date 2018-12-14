package com.dew.ed828.aihuaPlayer.download.fragment

import com.dew.ed828.aihuaPlayer.download.get.DownloadManager
import com.dew.ed828.aihuaPlayer.download.service.DownloadManagerService

/**
 *
 * Created by Edward on 12/12/2018.
 *
 */

class AllMissionsFragment : MissionsFragment() {

    override fun setupDownloadManager(binder: DownloadManagerService.DMBinder): DownloadManager {
        return binder.downloadManager!!
    }
}
