package com.dew.ed828.aihuaPlayer.download.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.util.Log
import android.widget.Toast
import com.dew.ed828.aihuaPlayer.about.R
import java.io.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and

/**
 *
 * Created by Edward on 12/11/2018.
 *
 */

object Utility {

    private const val TAG = "Download Utility"

    enum class FileType {
        VIDEO,
        MUSIC,
        UNKNOWN
    }

    fun formatBytes(bytes: Long): String =
        when {
            bytes < 1024 -> String.format("%d B", bytes)
            bytes < 1024 * 1024 -> String.format("%.2f kB", bytes.toFloat() / 1024)
            bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes.toFloat() / 1024f / 1024f)
            else -> String.format("%.2f GB", bytes.toFloat() / 1024f / 1024f / 1024f)
        }


    fun formatSpeed(speed: Float): String =
        when {
            speed < 1024 -> String.format("%.2f B/s", speed)
            speed < 1024 * 1024 -> String.format("%.2f kB/s", speed / 1024)
            speed < 1024 * 1024 * 1024 -> String.format("%.2f MB/s", speed / 1024f / 1024f)
            else -> String.format("%.2f GB/s", speed / 1024f / 1024f / 1024f)
        }


    fun writeToFile(fileName: String, serializable: Serializable) {
        var objectOutputStream: ObjectOutputStream? = null

        try {
            objectOutputStream = ObjectOutputStream(BufferedOutputStream(FileOutputStream(fileName)))
            objectOutputStream.writeObject(serializable)
        } catch (e: Exception) {
            Log.d(TAG, "writeToFile() failed: ${e.message}")

        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close()
                } catch (e: Exception) {
                    Log.d(TAG, "writeToFile() closing file failed: ${e.message}")
                }
            }
        }
    }

    fun <T> readFromFile(file: String): T? {
        var resultObject: T? = null
        var objectInputStream: ObjectInputStream? = null

        try {
            objectInputStream = ObjectInputStream(FileInputStream(file))
            resultObject = objectInputStream.readObject() as T
        } catch (e: Exception) {
            Log.d(TAG, "readFromFile() failed: ${e.message}")
        }

        if (objectInputStream != null) {
            try {
                objectInputStream.close()
            } catch (e: Exception) {
                Log.d(TAG, "readFromFile() closing file failed: ${e.message}")
            }
        }

        return resultObject
    }

    fun getFileExt(url: String): String? {
        var url = url
        var index: Int = url.indexOf("?")
        if (index > -1) {
            url = url.substring(0, index)
        }

        index = url.lastIndexOf(".")

        return if (index == -1) {
            null
        } else {
            var ext = url.substring(index)
            index = ext.indexOf("%")
            if (index > -1) {
                ext = ext.substring(0, index)
            }
            index = ext.indexOf("/")
            if (index > -1) {
                ext = ext.substring(0, index)
            }
            ext.toLowerCase()
        }
    }

    fun getFileType(file: String): FileType =
        when {
            (file.endsWith(".mp3") || file.endsWith(".wav") ||
                    file.endsWith(".flac") || file.endsWith(".m4a")) -> FileType.MUSIC

            (file.endsWith(".mp4") || file.endsWith(".mpeg") ||
                    file.endsWith(".rm") || file.endsWith(".rmvb") || file.endsWith(".flv") ||
                    file.endsWith(".webp") || file.endsWith(".webm")) -> FileType.VIDEO

            else -> FileType.UNKNOWN
        }


    @ColorRes
    fun getBackgroundForFileType(type: FileType): Int =
        when (type) {
            Utility.FileType.MUSIC -> R.color.audio_left_to_load_color
            Utility.FileType.VIDEO -> R.color.video_left_to_load_color
            else -> R.color.gray
        }


    @ColorRes
    fun getForegroundForFileType(type: FileType): Int =
        when (type) {
            Utility.FileType.MUSIC -> R.color.audio_already_load_color
            Utility.FileType.VIDEO -> R.color.video_already_load_color
            else -> R.color.gray
        }


    @DrawableRes
    fun getIconForFileType(type: FileType): Int =
        when (type) {
            Utility.FileType.MUSIC -> R.drawable.music
            Utility.FileType.VIDEO -> R.drawable.video
            else -> R.drawable.video
        }


    fun copyToClipboard(context: Context, str: String) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.primaryClip = ClipData.newPlainText("text", str)
        Toast.makeText(context, R.string.msg_copied, Toast.LENGTH_SHORT).show()
    }

    fun checksum(path: String, algorithm: String): String {
        val messageDigest: MessageDigest =
            try {
                MessageDigest.getInstance(algorithm)
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException(e)
            }

        val fileInputStream: FileInputStream =
            try {
                FileInputStream(path)
            } catch (e: FileNotFoundException) {
                throw RuntimeException(e)
            }

        val buf = ByteArray(1024)
        val length =
            try {
                fileInputStream.read(buf)

            } catch (ignored: IOException) {
                Log.d(TAG, "fileInputStream reading error: ${ignored.message}")
                throw RuntimeException(ignored)
            }

        while (length != -1) {
            messageDigest.update(buf, 0, length)
        }

        val digest = messageDigest.digest()

        // HEX
        val stringBuilder = StringBuilder()
        for (b in digest) {
            stringBuilder.append(Integer.toString((b and 0xff.toByte()) + 0x100, 16).substring(1))
        }

        return stringBuilder.toString()
    }
}
