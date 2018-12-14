package com.dew.ed828.aihuaPlayer.download.adapter

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.os.AsyncTask
import android.os.Build
import android.support.v4.content.FileProvider
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.download.get.DeleteDownloadManager
import com.dew.ed828.aihuaPlayer.download.get.DownloadManager
import com.dew.ed828.aihuaPlayer.download.model.DownloadMission
import com.dew.ed828.aihuaPlayer.download.service.DownloadManagerService
import com.dew.ed828.aihuaPlayer.download.util.ProgressDrawable
import com.dew.ed828.aihuaPlayer.download.util.Utility
import java.io.File
import java.lang.ref.WeakReference
import java.util.*

/**
 *
 * Created by Edward on 12/11/2018.
 *
 */

class MissionAdapter(private val mContext: Activity,
                     private val mBinder: DownloadManagerService.DMBinder?,
                     private val mDownloadManager: DownloadManager,
                     private val mDeleteDownloadManager: DeleteDownloadManager,
                     isLinear: Boolean) : RecyclerView.Adapter<MissionAdapter.ViewHolder>() {

    private val mInflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val mLayout: Int = if (isLinear) R.layout.mission_item_linear else R.layout.mission_item
    private val mItemList: MutableList<DownloadMission>

    init {
        mItemList = ArrayList()
        updateItemList()
    }

    fun updateItemList() {
        mItemList.clear()

        for (i in 0 until mDownloadManager.count) {
            val mission = mDownloadManager.getMission(i)
            if (!mDeleteDownloadManager.contains(mission)) {
                mItemList.add(mDownloadManager.getMission(i))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionAdapter.ViewHolder {
        val viewHolder = ViewHolder(mInflater.inflate(mLayout, parent, false))

        viewHolder.menu.setOnClickListener { buildPopup(viewHolder) }

//        viewHolder.itemView.setOnClickListener {
//				showDetail(viewHolder)
//			}

        return viewHolder
    }

    override fun onViewRecycled(viewHolder: MissionAdapter.ViewHolder) {
        super.onViewRecycled(viewHolder)
        viewHolder.mission!!.removeListener(viewHolder.observer)
        viewHolder.mission = null
        viewHolder.observer = null
        viewHolder.progress = null
        viewHolder.itemPosition = -1
        viewHolder.lastTimeStamp = -1
        viewHolder.lastDone = -1
        viewHolder.colorId = 0
    }

    override fun onBindViewHolder(viewHolder: MissionAdapter.ViewHolder, pos: Int) {
        val downloadMission = mItemList[pos]
        viewHolder.mission = downloadMission
        viewHolder.itemPosition = pos

        val type = Utility.getFileType(downloadMission.name)

        viewHolder.icon.setImageResource(Utility.getIconForFileType(type))
        viewHolder.name.text = downloadMission.name
        viewHolder.size.text = Utility.formatBytes(downloadMission.length)

        viewHolder.progress = ProgressDrawable(mContext, Utility.getBackgroundForFileType(type), Utility.getForegroundForFileType(type))
        ViewCompat.setBackground(viewHolder.bkg, viewHolder.progress)

        viewHolder.observer = MissionObserver(this, viewHolder)
        viewHolder.observer?.let {
            downloadMission.addListener(it)
        }

        updateProgress(viewHolder)
    }

    override fun getItemCount(): Int = mItemList.size


    override fun getItemId(position: Int): Long = position.toLong()


    private fun updateProgress(viewHolder: ViewHolder, finished: Boolean = false) {
        if (viewHolder.mission == null) return

        val now = System.currentTimeMillis()

        if (viewHolder.lastTimeStamp == -1L) {
            viewHolder.lastTimeStamp = now
        }

        if (viewHolder.lastDone == -1L) {
            viewHolder.lastDone = viewHolder.mission!!.done
        }

        val deltaTime = now - viewHolder.lastTimeStamp
        val deltaDone = viewHolder.mission!!.done - viewHolder.lastDone

        if (deltaTime == 0L || deltaTime > 1000 || finished) {
            if (viewHolder.mission!!.errCode > 0) {
                viewHolder.status.setText(R.string.msg_error)
            } else {
                val progress = viewHolder.mission!!.done.toFloat() / viewHolder.mission!!.length
                viewHolder.status.text = String.format(Locale.US, "%.2f%%", progress * 100)
                viewHolder.progress!!.setProgress(progress)
            }
        }

        if (deltaTime > 1000 && deltaDone > 0) {
            val speed = deltaDone.toFloat() / deltaTime
            val speedStr = Utility.formatSpeed(speed * 1000)
            val sizeStr = Utility.formatBytes(viewHolder.mission!!.length)
            val string = "$sizeStr $speedStr"

            viewHolder.size.text = string
            viewHolder.lastTimeStamp = now
            viewHolder.lastDone = viewHolder.mission!!.done
        }
    }


    private fun buildPopup(viewHolder: ViewHolder) {
        val popup = PopupMenu(mContext, viewHolder.menu)
        popup.inflate(R.menu.menu_mission)

        val menu = popup.menu
        val start = menu.findItem(R.id.start)
        val pause = menu.findItem(R.id.pause)
        val view = menu.findItem(R.id.view)
        val delete = menu.findItem(R.id.delete)
        val checksum = menu.findItem(R.id.checksum)

        // Set to false first
        start.isVisible = false
        pause.isVisible = false
        view.isVisible = false
        delete.isVisible = false
        checksum.isVisible = false

        if (!viewHolder.mission!!.finished) {
            if (!viewHolder.mission!!.running) {
                if (viewHolder.mission!!.errCode == -1) {
                    start.isVisible = true
                }

                delete.isVisible = true
            } else {
                pause.isVisible = true
            }
        } else {
            view.isVisible = true
            delete.isVisible = true
            checksum.isVisible = true
        }

        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            val id = item.itemId
            when (id) {
                R.id.start -> {
                    mDownloadManager.resumeMission(viewHolder.itemPosition)
                    mBinder!!.onMissionAdded(mItemList[viewHolder.itemPosition])
                    true
                }

                R.id.pause -> {
                    mDownloadManager.pauseMission(viewHolder.itemPosition)
                    mBinder!!.onMissionRemoved(mItemList[viewHolder.itemPosition])
                    viewHolder.lastTimeStamp = -1
                    viewHolder.lastDone = -1
                    true
                }

                R.id.view -> {
                    val file = File(viewHolder.mission!!.location, viewHolder.mission!!.name)
                    val ext = Utility.getFileExt(viewHolder.mission!!.name)

                    Log.d(TAG, "Viewing file: ${file.absolutePath} ext: $ext")

                    if (ext == null) {
                        Log.w(TAG, "Can't view file because it has no extension: ${viewHolder.mission!!.name}")
                        return@OnMenuItemClickListener false
                    }

                    val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.substring(1))
                    Log.v(TAG, "Mime: $mime package: ${mContext.applicationContext.packageName}.provider")

                    if (file.exists()) {
                        viewFileWithFileProvider(file, mime)
                    } else {
                        Log.w(TAG, "File doesn't exist")
                    }

                    true
                }

                R.id.delete -> {
                    mDeleteDownloadManager.add(viewHolder.mission!!)
                    updateItemList()
                    notifyDataSetChanged()
                    true
                }

                R.id.md5, R.id.sha1 -> {
                    val mission = mItemList[viewHolder.itemPosition]
                    ChecksumTask(mContext).execute("${mission.location}/${mission.name}", ALGORITHMS[id])
                    true
                }

                else -> false
            }
        })

        popup.show()
    }

    private fun viewFileWithFileProvider(file: File, mimetype: String?) {
        val ourPackage = mContext.applicationContext.packageName
        val uri = FileProvider.getUriForFile(mContext, "$ourPackage.provider", file)
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(uri, mimetype)
        intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(FLAG_GRANT_PREFIX_URI_PERMISSION)
        }
        //mContext.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Log.v(TAG, "Starting intent: $intent")
        if (intent.resolveActivity(mContext.packageManager) != null) {
            mContext.startActivity(intent)
        } else {
            Toast.makeText(mContext, R.string.toast_no_player, Toast.LENGTH_LONG).show()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mission: DownloadMission? = null
        var itemPosition: Int = 0

        val status: TextView = view.findViewById(R.id.item_status)
        val icon: ImageView = view.findViewById(R.id.item_icon)
        val name: TextView = view.findViewById(R.id.item_name)
        val size: TextView = view.findViewById(R.id.item_size)
        val bkg: View = view.findViewById(R.id.item_bkg)
        val menu: ImageView = view.findViewById(R.id.item_more)
        var progress: ProgressDrawable? = null
        var observer: MissionObserver? = null

        var lastTimeStamp: Long = -1
        var lastDone: Long = -1
        var colorId: Int = 0

    }

    class MissionObserver(private val mAdapter: MissionAdapter, private val mHolder: ViewHolder) : DownloadMission.MissionListener {

        override fun onProgressUpdate(downloadMission: DownloadMission, done: Long, total: Long) {
            mAdapter.updateProgress(mHolder)
        }

        override fun onFinish(downloadMission: DownloadMission) {
            //mAdapter.mManager.deleteMission(mHolder.position);
            // TODO Notification
            //mAdapter.notifyDataSetChanged();
            if (mHolder.mission != null) {
                mHolder.size.text = Utility.formatBytes(mHolder.mission!!.length)
                mAdapter.updateProgress(mHolder, true)
            }
        }

        override fun onError(downloadMission: DownloadMission, errCode: Int) {
            mAdapter.updateProgress(mHolder)
        }

    }

    private class ChecksumTask(activity: Activity) : AsyncTask<String, Void, String>() {
        internal var prog: ProgressDialog? = null
        internal val weakReference: WeakReference<Activity> = WeakReference(activity)

        private val activity: Activity?
            get() {
                val activity = weakReference.get()

                return if (activity != null && activity.isFinishing) {
                    null
                } else {
                    activity
                }
            }

        override fun onPreExecute() {
            super.onPreExecute()

            val activity = activity
            if (activity != null) {
                // Create dialog
                prog = ProgressDialog(activity)
                prog?.let {
                    it.setCancelable(false)
                    it.setMessage(activity.getString(R.string.msg_wait))
                    it.show()
                }
            }
        }

        override fun doInBackground(vararg params: String): String {
            return Utility.checksum(params[0], params[1])
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            prog?.let {
                Utility.copyToClipboard(it.context, result)
                if (activity != null) {
                    it.dismiss()
                }
            }
        }
    }

    companion object {
        private val ALGORITHMS = HashMap<Int, String>()
        private const val TAG = "MissionAdapter"

        init {
            ALGORITHMS[R.id.md5] = "MD5"
            ALGORITHMS[R.id.sha1] = "SHA1"
        }
    }
}
