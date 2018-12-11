package com.dew.ed828.aihuaPlayer.player.Helper

import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import android.os.PowerManager
import android.util.Log

/**
 *
 * Created by Edward on 12/7/2018.
 *
 * lock wake and wifi, including cpu
 */

class LockManager(context: Context) {


    private val powerManager: PowerManager = context.applicationContext.getSystemService(POWER_SERVICE) as PowerManager
    private val wifiManager: WifiManager = context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null

    fun acquireWifiAndCpu() {
        Log.d(TAG, "acquireWifiAndCpu() called")
        if (wakeLock != null && wakeLock!!.isHeld && wifiLock != null && wifiLock!!.isHeld) return

        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG)
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, TAG)

        wakeLock?.acquire()
        wifiLock?.acquire()
    }

    fun releaseWifiAndCpu() {
        Log.d(TAG, "releaseWifiAndCpu() called")
        if (wakeLock != null && wakeLock!!.isHeld) wakeLock!!.release()
        if (wifiLock != null && wifiLock!!.isHeld) wifiLock!!.release()

        wakeLock = null
        wifiLock = null
    }

    companion object {
        private val TAG = "LockManager@" + hashCode()
    }
}
