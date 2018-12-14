package com.dew.ed828.aihuaPlayer.download.fragment

import android.app.Activity

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*

import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.download.adapter.MissionAdapter
import com.dew.ed828.aihuaPlayer.download.get.DeleteDownloadManager
import com.dew.ed828.aihuaPlayer.download.get.DownloadManager
import com.dew.ed828.aihuaPlayer.download.service.DownloadManagerService
import io.reactivex.disposables.Disposable


abstract class MissionsFragment : Fragment() {
    private lateinit var mDownloadManager: DownloadManager
    private var mBinder: DownloadManagerService.DMBinder? = null

    private var mPrefs: SharedPreferences? = null
    private var isLinearLayout: Boolean = false
    private var mSwitch: MenuItem? = null

    private var mList: RecyclerView? = null
    private var mAdapter: MissionAdapter? = null
    private var mGridManager: GridLayoutManager? = null
    private var mLinearManager: LinearLayoutManager? = null
    private lateinit var mActivity: Context
    private lateinit var mDeleteDownloadManager: DeleteDownloadManager
    private var mDeleteDisposable: Disposable? = null

    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            mBinder = binder as DownloadManagerService.DMBinder
            mDownloadManager = setupDownloadManager(mBinder!!)

            mDeleteDownloadManager.setDownloadManager(mDownloadManager)
            updateList()

        }

        override fun onServiceDisconnected(name: ComponentName) {
            // What to do?
        }


    }

    fun setDeleteManager(deleteDownloadManager: DeleteDownloadManager) {
        mDeleteDownloadManager = deleteDownloadManager

        mDeleteDownloadManager.setDownloadManager(mDownloadManager)
        updateList()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_missions, container, false)

        mPrefs = PreferenceManager.getDefaultSharedPreferences(activity)
        isLinearLayout = mPrefs!!.getBoolean("linear", false)

        // Bind the service
        val i = Intent()
        i.setClass(activity as Context, DownloadManagerService::class.java)
        activity?.bindService(i, mConnection, Context.BIND_AUTO_CREATE)

        // Views
        mList = view.findViewById(R.id.mission_recycler)

        // Init
        mGridManager = GridLayoutManager(activity, 2)
        mLinearManager = LinearLayoutManager(activity)
        mList!!.layoutManager = mGridManager

        setHasOptionsMenu(true)

        return view
    }

    /**
     * Added in API level 23.
     */
    override fun onAttach(activity: Context) {
        super.onAttach(activity)

        // Bug: in api< 23 this is never called
        // so mActivity=null
        // so app crashes with nullpointer exception
        mActivity = activity
    }

    /**
     * deprecated in API level 23,
     * but must remain to allow compatibility with api<23
     */
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)

        mActivity = activity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mDeleteDisposable = mDeleteDownloadManager.undoObservable.subscribe { mission ->
            mAdapter?.let {
                it.updateItemList()
                it.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.unbindService(mConnection)

        mDeleteDisposable?.dispose()

    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        mSwitch = menu.findItem(R.id.switch_mode)
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.switch_mode -> {
                isLinearLayout = !isLinearLayout
                updateList()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }


    fun notifyChange() {
        mAdapter?.notifyDataSetChanged()
    }

    private fun updateList() {
        mAdapter = MissionAdapter(mActivity as Activity, mBinder, mDownloadManager, mDeleteDownloadManager, isLinearLayout)

        if (isLinearLayout) {
            mList!!.layoutManager = mLinearManager
        } else {
            mList!!.layoutManager = mGridManager
        }

        mList!!.adapter = mAdapter

        if (mSwitch != null) {
            mSwitch!!.setIcon(if (isLinearLayout) R.drawable.grid else R.drawable.list)
        }

        mPrefs!!.edit().putBoolean("linear", isLinearLayout).apply()
    }

    protected abstract fun setupDownloadManager(binder: DownloadManagerService.DMBinder): DownloadManager
}
