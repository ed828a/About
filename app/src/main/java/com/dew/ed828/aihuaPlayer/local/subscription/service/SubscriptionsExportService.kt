package com.dew.ed828.aihuaPlayer.local.subscription.service

import android.app.Service
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.text.TextUtils
import android.util.Log
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.local.subscription.helper.ImportExportJsonHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import org.schabi.newpipe.extractor.subscription.SubscriptionItem
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream

/**
 * Created by Edward on 12/13/2018.
 */

class SubscriptionsExportService : BaseImportExportService() {

    private var subscription: Subscription? = null
    private var outFile: File? = null
    private var outputStream: FileOutputStream? = null

    private val subscriber: Subscriber<File>
        get() = object : Subscriber<File> {
            override fun onSubscribe(s: Subscription) {
                subscription = s
                s.request(1)
            }

            override fun onNext(file: File) {
                Log.d(TAG, "startExport() success: file = $file")
            }

            override fun onError(error: Throwable) {
                Log.e(TAG, "onError() called with: error = [$error]", error)
                handleError(error)
            }

            override fun onComplete() {
                LocalBroadcastManager.getInstance(this@SubscriptionsExportService).sendBroadcast(
                    Intent(
                    EXPORT_COMPLETE_ACTION
                )
                )
                showToast(R.string.export_complete_toast)
                stopService()
            }
        }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null || subscription != null) return Service.START_NOT_STICKY

        val path = intent.getStringExtra(KEY_FILE_PATH)
        if (TextUtils.isEmpty(path)) {
            stopAndReportError(IllegalStateException("Exporting to a file, but the path is empty or null"), "Exporting subscriptions")
            return Service.START_NOT_STICKY
        }

        try {
            outFile = File(path)
            outputStream = FileOutputStream(outFile)
        } catch (e: FileNotFoundException) {
            handleError(e)
            return Service.START_NOT_STICKY
        }

        startExport()

        return Service.START_NOT_STICKY
    }

    override fun getNotificationId(): Int {
        return 4567
    }

    override fun getTitle(): Int {
        return R.string.export_ongoing
    }

    override fun disposeAll() {
        super.disposeAll()
        if (subscription != null) subscription!!.cancel()
    }

    private fun startExport() {
        showToast(R.string.export_ongoing)

        subscriptionService.subscriptionTable()
            .all
            .take(1)
            .map<List<SubscriptionItem>> { subscriptionEntities ->
                val result = ArrayList<SubscriptionItem>(subscriptionEntities.size)
                for (entity in subscriptionEntities) {
                    result.add(SubscriptionItem(entity.serviceId, entity.url, entity.name))
                }
                result
            }
            .map(exportToFile())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(subscriber)
    }

    private fun exportToFile(): Function<List<SubscriptionItem>, File> {
        return Function{ subscriptionItems ->
            outputStream?.let {
                ImportExportJsonHelper.writeTo(subscriptionItems, it, eventListener)
            }
            outFile
        }
    }

    protected fun handleError(error: Throwable) {
        super.handleError(R.string.subscriptions_export_unsuccessful, error)
    }

    companion object {
        const val KEY_FILE_PATH = "key_file_path"

        /**
         * A [local broadcast][LocalBroadcastManager] will be made with this action when the export is successfully completed.
         */
        const val EXPORT_COMPLETE_ACTION = "org.schabi.newpipe.local.subscription.services.SubscriptionsExportService.EXPORT_COMPLETE"
    }
}