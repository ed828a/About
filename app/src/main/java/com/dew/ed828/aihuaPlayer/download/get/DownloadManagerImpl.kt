package com.dew.ed828.aihuaPlayer.download.get

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import com.dew.ed828.aihuaPlayer.download.model.DownloadMission
import com.dew.ed828.aihuaPlayer.download.util.Utility
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 *
 * Created by Edward on 12/11/2018.
 *
 */
/**
 * Create a new instance
 *
 * @param searchLocations    the directories to search for unfinished downloads
 * @param downloadDataSource the data source for finished downloads
 */
class DownloadManagerImpl(searchLocations: Collection<String>, private val mDownloadDataSource: DownloadDataSource, private val context: Context? = null) : DownloadManager {

    private val mMissions = ArrayList<DownloadMission>()
    init {
        loadMissions(searchLocations)
    }

    override fun startMission(url: String, location: String, name: String, isAudio: Boolean, threads: Int): Int {
        var localName = name
        val existingMission = getMissionByLocation(location, localName)
        if (existingMission != null) {
            // Already downloaded or downloading
            if (existingMission.finished) {
                // Overwrite mission
                deleteMission(mMissions.indexOf(existingMission))
            } else {
                // Rename file (?)
                try {
                    localName = generateUniqueName(location, localName)
                } catch (e: Exception) {
                    Log.e(TAG, "Unable to generate unique name, reason: ${e.message}")
                    localName = System.currentTimeMillis().toString() + localName
                    Log.i(TAG, "Using $localName")
                }

            }
        }

        val mission = DownloadMission(localName, url, location)
        mission.timestamp = System.currentTimeMillis()
        mission.threadCount = threads
        mission.addListener(MissionListener(mission))
        Initializer(mission).start()
        return insertMission(mission)
    }

    /**
     * @param i: mission index
     */
    override fun resumeMission(i: Int) {
        val d = getMission(i)
        if (!d.running && d.errCode == -1) {
            d.start()
        }
    }

    override fun pauseMission(i: Int) {
        val downloadMission = getMission(i)
        if (downloadMission.running) {
            downloadMission.pause()
        }
    }

    override fun deleteMission(i: Int) {
        val mission = getMission(i)
        if (mission.finished) {
            mDownloadDataSource.deleteMission(mission)
        }
        mission.delete()
        mMissions.removeAt(i)
    }

    private fun loadMissions(searchLocations: Iterable<String>) {
        mMissions.clear()
        loadFinishedMissions()
        for (location in searchLocations) {
            loadMissions(location)
        }

    }

    /**
     * Loads finished missions from the data source
     */
    private fun loadFinishedMissions() {
        var finishedMissions: List<DownloadMission>? = mDownloadDataSource.loadMissions()
        if (finishedMissions == null) {
            finishedMissions = ArrayList()
        }
        // Ensure its sorted
        sortByTimestamp(finishedMissions)

        mMissions.ensureCapacity(mMissions.size + finishedMissions.size)
        for (mission in finishedMissions) {
            val downloadedFile = mission.downloadedFile
            if (!downloadedFile.isFile) {
                Log.d(TAG, "downloaded file removed: " + downloadedFile.absolutePath)

                mDownloadDataSource.deleteMission(mission)
            } else {
                mission.length = downloadedFile.length()
                mission.finished = true
                mission.running = false
                mMissions.add(mission)
            }
        }
    }

    private fun loadMissions(location: String) {

        val f = File(location)

        if (f.exists() && f.isDirectory) {
            val subs = f.listFiles()

            if (subs == null) {
                Log.e(TAG, "listFiles() returned null")
                return
            }

            for (sub in subs) {
                if (sub.isFile && sub.name.endsWith(".giga")) {
                    val mis = Utility.readFromFile<DownloadMission>(sub.absolutePath)
                    if (mis != null) {
                        if (mis.finished) {
                            if (!sub.delete()) {
                                Log.w(TAG, "Unable to delete .giga file: " + sub.path)
                            }
                            continue
                        }

                        mis.running = false
                        mis.recovered = true
                        insertMission(mis)
                    }
                }
            }
        }
    }

    override fun getMission(i: Int): DownloadMission {
        return mMissions[i]
    }

    override val count: Int
        get() = mMissions.size

    private fun insertMission(mission: DownloadMission): Int {
        var i = -1

        var downloadMission: DownloadMission? = null

        if (mMissions.size > 0) {
            do {
                downloadMission = mMissions[++i]
            } while (downloadMission!!.timestamp > mission.timestamp && i < mMissions.size - 1)

            //if (i > 0) i--;
        } else {
            i = 0
        }

        mMissions.add(i, mission)

        return i
    }

    /**
     * Get a mission by its location and name
     *
     * @param location the location
     * @param name     the name
     * @return the mission or null if no such mission exists
     */
    private fun getMissionByLocation(location: String, name: String): DownloadMission? {
        for (mission in mMissions) {
            if (location == mission.location && name == mission.name) {
                return mission
            }
        }
        return null
    }

    private inner class Initializer(private val mission: DownloadMission) : Thread() {
        private val handler: Handler = Handler()

        override fun run() {
            try {
                val url = URL(mission.url)
                var conn = url.openConnection() as HttpURLConnection
                mission.length = conn.contentLength.toLong()

                if (mission.length <= 0) {
                    mission.errCode = DownloadMission.ERROR_SERVER_UNSUPPORTED
                    //mission.notifyError(DownloadMission.ERROR_SERVER_UNSUPPORTED);
                    return
                }

                // Open again,todo: Why? Can't see the reason
                conn = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("Range", "bytes=${mission.length - 10}-${mission.length}")

                if (conn.responseCode != 206) {
                    // Fallback to single thread if no partial content support
                    mission.fallback = true

                    Log.d(TAG, "downloading is falling back")
                }

                Log.d(TAG, "response = ${conn.responseCode}")

                mission.blocks = mission.length / DownloadManager.BLOCK_SIZE

                if (mission.threadCount > mission.blocks) {
                    mission.threadCount = mission.blocks.toInt()
                }

                if (mission.threadCount <= 0) {
                    mission.threadCount = 1
                }

                if (mission.blocks * DownloadManager.BLOCK_SIZE < mission.length) {
                    mission.blocks = mission.blocks + 1
                }


                File(mission.location).mkdirs()
                File("${mission.location}/${mission.name}").createNewFile()
                val accessFile = RandomAccessFile("${mission.location}/${mission.name}", "rw")
                accessFile.setLength(mission.length)
                accessFile.close()

                mission.start()
            } catch (ie: IOException) {
                if (context == null) throw RuntimeException(ie)

                if (ie.message != null && ie.message!!.contains("Permission denied")) {
                    handler.post { context.startActivity(Intent(context, ExtSDDownloadFailedActivity::class.java)) }
                } else
                    throw RuntimeException(ie)
            } catch (e: Exception) {
                // TODO Notify
                throw RuntimeException(e)
            }

        }
    }

    /**
     * Waits for mission to finish to add it to the [.mDownloadDataSource]
     */
    private inner class MissionListener(private val mMission: DownloadMission) : DownloadMission.MissionListener {

        override fun onProgressUpdate(downloadMission: DownloadMission, done: Long, total: Long) {}

        override fun onFinish(downloadMission: DownloadMission) {
            mDownloadDataSource.addMission(mMission)
        }

        override fun onError(downloadMission: DownloadMission, errCode: Int) {}
    }

    companion object {
        private val TAG = DownloadManagerImpl::class.java.simpleName

        /**
         * Sort a list of mission by its timestamp. Oldest first
         * @param missions the missions to sort
         */
        internal fun sortByTimestamp(missions: List<DownloadMission>) {
            Collections.sort(missions) { o1, o2 -> java.lang.Long.compare(o1.timestamp, o2.timestamp) }
        }

        /**
         * Splits the filename into name and extension
         *
         *
         * Dots are ignored if they appear: not at all, at the beginning of the file,
         * at the end of the file
         *
         * @param name the name to split
         * @return a string array with a length of 2 containing the name and the extension
         */
        private fun splitName(name: String): Array<String> {
            val dotIndex = name.lastIndexOf('.')
            return if (dotIndex <= 0 || dotIndex == name.length - 1) {
                arrayOf(name, "")
            } else {
                arrayOf(name.substring(0, dotIndex), name.substring(dotIndex + 1))
            }
        }

        /**
         * Generates a unique file name.
         *
         *
         * e.g. "myname (1).txt" if the name "myname.txt" exists.
         *
         * @param location the location (to check for existing files)
         * @param name     the name of the file
         * @return the unique file name
         * @throws IllegalArgumentException if the location is not a directory
         * @throws SecurityException        if the location is not readable
         */
        private fun generateUniqueName(location: String?, name: String?): String {
            if (location == null) throw NullPointerException("location is null")
            if (name == null) throw NullPointerException("name is null")
            val destination = File(location)
            if (!destination.isDirectory) {
                throw IllegalArgumentException("location is not a directory: $location")
            }
            val nameParts = splitName(name)
            val existingName = destination.list { dir, name -> name.startsWith(nameParts[0]) }
            Arrays.sort(existingName)
            var newName: String
            var downloadIndex = 0
            do {
                newName = nameParts[0] + " (" + downloadIndex + ")." + nameParts[1]
                ++downloadIndex
                if (downloadIndex == 1000) {  // Probably an error on our side
                    throw RuntimeException("Too many existing files")
                }
            } while (Arrays.binarySearch(existingName, newName) >= 0)
            return newName
        }
    }
}
