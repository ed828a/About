package com.dew.ed828.aihuaPlayer.download.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.settings.EdPlayerSettings
import com.dew.ed828.aihuaPlayer.util.ServiceHelper
import com.dew.ed828.aihuaPlayer.util.ThemeHelper

class ExtSDDownloadFailedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeHelper.setTheme(this, ServiceHelper.getSelectedServiceId(this))
    }

    override fun onStart() {
        super.onStart()
        AlertDialog.Builder(this)
            .setTitle(R.string.download_to_sdcard_error_title)
            .setMessage(R.string.download_to_sdcard_error_message)
            .setPositiveButton(R.string.yes) { dialogInterface: DialogInterface, i: Int ->
                EdPlayerSettings.resetDownloadFolders(this)
                finish()
            }
            .setNegativeButton(R.string.cancel) { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss()
                finish()
            }
            .create()
            .show()
    }
}
