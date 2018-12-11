package com.dew.ed828.aihuaPlayer.download.runnable

import com.dew.ed828.aihuaPlayer.download.model.DownloadMission
import java.io.BufferedInputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL

/**
 *
 * Created by Edward on 12/11/2018.
 *
 * Single-threaded fallback mode
 */

class DownloadRunnableFallback(private val mMission: DownloadMission) : Runnable {

    override fun run() {
        try {
            val url = URL(mMission.url)
            val conn = url.openConnection() as HttpURLConnection

            if (conn.responseCode != 200 && conn.responseCode != 206) {
                notifyError(DownloadMission.ERROR_SERVER_UNSUPPORTED)
            } else {
                val file = RandomAccessFile("${mMission.location}/${mMission.name}", "rw")
                file.seek(0)
                val ipt = BufferedInputStream(conn.inputStream)
                val buf = ByteArray(512)
                val len = ipt.read(buf, 0, 512)
                while (len != -1 && mMission.running) {
                    file.write(buf, 0, len)
                    notifyProgress(len.toLong())

                    if (Thread.interrupted()) {
                        break
                    }
                }

                file.close()
                ipt.close()
            }
        } catch (e: Exception) {
            notifyError(DownloadMission.ERROR_UNKNOWN)
        }

        if (mMission.errCode == -1 && mMission.running) {
            notifyFinished()
        }
    }

    private fun notifyProgress(len: Long) {
        synchronized(mMission) {
            mMission.notifyProgress(len)
        }
    }

    private fun notifyError(err: Int) {
        synchronized(mMission) {
            mMission.notifyError(err)
            mMission.pause()
        }
    }

    private fun notifyFinished() {
        synchronized(mMission) {
            mMission.notifyFinished()
        }
    }
}
