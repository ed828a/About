package com.dew.ed828.aihuaPlayer.download.get

import android.app.Activity
import android.os.Bundle
import android.support.design.widget.BaseTransientBottomBar
import android.support.design.widget.Snackbar
import android.view.View
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.download.model.DownloadMission
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

/**
 *
 * Created by Edward on 12/11/2018.
 *
 */

class DeleteDownloadManager(activity: Activity) {

    private val mView: View = activity.findViewById(android.R.id.content)
    private val mPendingMap: HashSet<String> = HashSet()
    private val mDisposableList: MutableList<Disposable> = ArrayList()
    private var mDownloadManager: DownloadManager? = null
    private val publishSubject = PublishSubject.create<DownloadMission>()

    val undoObservable: Observable<DownloadMission>
        get() = publishSubject

    operator fun contains(mission: DownloadMission): Boolean {
        return mPendingMap.contains(mission.url)
    }

    fun add(mission: DownloadMission) {
        mPendingMap.add(mission.url)

        if (mPendingMap.size == 1) {
            showUndoDeleteSnackbar(mission)
        }
    }

    fun setDownloadManager(downloadManager: DownloadManager) {
        mDownloadManager = downloadManager

        if (mPendingMap.size < 1) return

        showUndoDeleteSnackbar()
    }

    fun restoreState(savedInstanceState: Bundle?) {
        val list = savedInstanceState?.getStringArrayList(KEY_STATE)

        list?.let {
            mPendingMap.addAll(it)
        }
    }

    fun saveState(outState: Bundle?) {
        if (outState == null) return

        for (disposable in mDisposableList) {
            disposable.dispose()
        }

        outState.putStringArrayList(KEY_STATE, ArrayList(mPendingMap))
    }

    private fun showUndoDeleteSnackbar() {
        if (mPendingMap.size < 1) return

        val url = mPendingMap.iterator().next()

        for (i in 0 until mDownloadManager!!.count) {
            val mission = mDownloadManager!!.getMission(i)
            if (url == mission.url) {
                showUndoDeleteSnackbar(mission)
                break
            }
        }
    }

    private fun showUndoDeleteSnackbar(mission: DownloadMission) {
        val snackbar = Snackbar.make(mView, mission.name, Snackbar.LENGTH_INDEFINITE)
        val disposable = Observable.timer(3, TimeUnit.SECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe { l -> snackbar.dismiss() }

        mDisposableList.add(disposable)

        snackbar.setAction(R.string.undo) { v ->
            mPendingMap.remove(mission.url)
            publishSubject.onNext(mission)
            disposable.dispose()
            snackbar.dismiss()
        }

        snackbar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                if (!disposable.isDisposed) {
                    Completable.fromAction { deletePending(mission) }
                        .subscribeOn(Schedulers.io())
                        .subscribe()
                }
                mPendingMap.remove(mission.url)
                snackbar.removeCallback(this)
                mDisposableList.remove(disposable)
                showUndoDeleteSnackbar()
            }
        })

        snackbar.show()
    }

    fun deletePending() {
        if (mPendingMap.size < 1) return

        val idSet = HashSet<Int>()
        for (i in 0 until mDownloadManager!!.count) {
            if (contains(mDownloadManager!!.getMission(i))) {
                idSet.add(i)
            }
        }

        for (id in idSet) {
            mDownloadManager!!.deleteMission(id)
        }

        mPendingMap.clear()
    }

    private fun deletePending(mission: DownloadMission) {
        for (i in 0 until mDownloadManager!!.count) {
            if (mission.url == mDownloadManager!!.getMission(i).url) {
                mDownloadManager!!.deleteMission(i)
                break
            }
        }
    }

    companion object {

        private const val KEY_STATE = "delete_manager_state"
    }
}
