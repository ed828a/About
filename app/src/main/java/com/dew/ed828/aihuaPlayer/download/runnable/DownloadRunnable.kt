package com.dew.ed828.aihuaPlayer.download.runnable


import android.util.Log
import com.dew.ed828.aihuaPlayer.download.get.DownloadManager
import com.dew.ed828.aihuaPlayer.download.model.DownloadMission
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL

/**
 *
 * Created by Edward on 12/11/2018.
 *
 * Runnable to download blocks of a file until the file is completely downloaded,
 * an error occurs or the process is stopped.
 */
class DownloadRunnable(private val mMission: DownloadMission, private val mId: Int) : Runnable {

    override fun run() {
        var retry = mMission.recovered
        var position = mMission.getPosition(mId)

        Log.d(TAG, "$mId: default pos $position, -- recovered: ${mMission.recovered}")

        while (mMission.errCode == -1 && mMission.running && position < mMission.blocks) {
            if (Thread.currentThread().isInterrupted) {
                mMission.pause()
                return
            }

            Log.d(TAG, mId.toString() + ":retry is $retry. Resuming at " + position)

            // Wait for an unblocked position
            while (!retry && position < mMission.blocks && mMission.isBlockPreserved(position)) {
                Log.d(TAG, mId.toString() + ":position " + position + " preserved, passing")

                position++
            }

            retry = false

            if (position >= mMission.blocks) {
                break
            }

            Log.d(TAG, mId.toString() + ":preserving position " + position)

            mMission.preserveBlock(position)
            mMission.setPosition(mId, position)

            var start = position * DownloadManager.BLOCK_SIZE
            var end = start + DownloadManager.BLOCK_SIZE - 1

            if (end >= mMission.length) {
                end = mMission.length - 1
            }



            var total = 0

            try {
                val url = URL(mMission.url)
                val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("Range", "bytes=$start-$end")

                Log.d(TAG, "${mId.toString()}:${conn.getRequestProperty("Range")} -- Content-Length=${conn.contentLength} Code:${conn.responseCode}")

                // A server may be ignoring the range request
                if (conn.responseCode != 206) {
                    mMission.errCode = DownloadMission.ERROR_SERVER_UNSUPPORTED
                    notifyError()

                    Log.e(TAG, "${mId.toString()}:Unsupported ${conn.responseCode}")

                    break
                }

                val file = RandomAccessFile("${mMission.location}/${mMission.name}", "rw")
                file.seek(start)
                val ipt = conn.inputStream
                val buf = ByteArray(64 * 1024)

                while (start < end && mMission.running) {
                    val len = ipt.read(buf, 0, buf.size)

                    if (len == -1) {
                        break
                    } else {
                        start += len.toLong()
                        total += len
                        file.write(buf, 0, len)
                        notifyProgress(len.toLong())
                    }
                }

                Log.d(TAG, "$mId:position $position finished, total length $total")

                file.close()
                ipt.close()

                // TODO We should save progress for each thread
            } catch (e: Exception) {
                // TODO Retry count limit & notify error
                retry = true

                notifyProgress((-total).toLong())

                Log.d(TAG, "$mId:position $position retrying, exception: ${e.message}")
            }

        }

        Log.d(TAG, "thread $mId exited main loop")

        if (mMission.errCode == -1 && mMission.running) {
            Log.d(TAG, "no error has happened, notifying")
            notifyFinished()
        }

        Log.d(TAG, "The mission has been paused. Passing.")
    }

    private fun notifyProgress(len: Long) {
        synchronized(mMission) {
            mMission.notifyProgress(len)
        }
    }

    private fun notifyError() {
        synchronized(mMission) {
            mMission.notifyError(DownloadMission.ERROR_SERVER_UNSUPPORTED)
            mMission.pause()
        }
    }

    private fun notifyFinished() {
        synchronized(mMission) {
            mMission.notifyFinished()
        }
    }

    companion object {
        private val TAG = DownloadRunnable::class.java.simpleName
    }
}
