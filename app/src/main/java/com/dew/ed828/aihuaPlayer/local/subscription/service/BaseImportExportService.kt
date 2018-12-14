package com.dew.ed828.aihuaPlayer.local.subscription.service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.annotation.StringRes
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.text.TextUtils
import android.widget.Toast
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.local.subscription.helper.SubscriptionService
import com.dew.ed828.aihuaPlayer.local.subscription.listener.ImportExportEventListener
import com.dew.ed828.aihuaPlayer.report.activity.ErrorActivity
import com.dew.ed828.aihuaPlayer.report.model.ErrorInfo
import com.dew.ed828.aihuaPlayer.report.model.UserAction
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import org.reactivestreams.Publisher
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor
import java.io.FileNotFoundException
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import io.reactivex.functions.Function

abstract class BaseImportExportService : Service() {
    protected val TAG = this.javaClass.simpleName

    protected lateinit var notificationManager: NotificationManagerCompat
    protected lateinit var notificationBuilder: NotificationCompat.Builder

    protected lateinit var subscriptionService: SubscriptionService
    protected val disposables = CompositeDisposable()
    protected val notificationUpdater = PublishProcessor.create<String>()

    protected val currentProgress = AtomicInteger(-1)
    protected val maxProgress = AtomicInteger(-1)
    protected val eventListener: ImportExportEventListener = object : ImportExportEventListener {
        override fun onSizeReceived(size: Int) {
            maxProgress.set(size)
            currentProgress.set(0)
        }

        override fun onItemCompleted(itemName: String) {
            currentProgress.incrementAndGet()
            notificationUpdater.onNext(itemName)
        }
    }

    protected abstract fun getNotificationId(): Int

    @StringRes
    abstract fun getTitle(): Int

    ///////////////////////////////////////////////////////////////////////////
    // Toast
    ///////////////////////////////////////////////////////////////////////////

    protected var toast: Toast? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        subscriptionService = SubscriptionService.getInstance(this)
        setupNotification()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposeAll()
    }

    protected open fun disposeAll() {
        disposables.clear()
    }

    protected fun setupNotification() {
        notificationManager = NotificationManagerCompat.from(this)
        notificationBuilder = createNotification()
        startForeground(getNotificationId(), notificationBuilder.build())

        val throttleAfterFirstEmission: Function<Flowable<String>, Publisher<String>>  = Function{ flow ->
            flow.limit(1)
                .concatWith(flow.skip(1).throttleLast(NOTIFICATION_SAMPLING_PERIOD.toLong(), TimeUnit.MILLISECONDS))
        }

        disposables.add(notificationUpdater
            .filter { s -> !s.isEmpty() }
            .publish<String>(throttleAfterFirstEmission)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { this.updateNotification(it) })
    }

    protected fun updateNotification(text: String) {
        var localText = text
        notificationBuilder.setProgress(maxProgress.get(), currentProgress.get(), maxProgress.get() == -1)

        val progressText = currentProgress.toString() + "/" + maxProgress
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!TextUtils.isEmpty(text)) localText = "$text  ($progressText)"
        } else {
            notificationBuilder.setContentInfo(progressText)
        }

        if (!TextUtils.isEmpty(localText)) notificationBuilder.setContentText(localText)
        notificationManager.notify(getNotificationId(), notificationBuilder.build())
    }

    protected fun stopService() {
        postErrorResult(null, null)
    }

    protected fun stopAndReportError(error: Throwable?, request: String) {
        stopService()

        val errorInfo = ErrorInfo.make(
            UserAction.SUBSCRIPTION, "unknown",
            request, R.string.general_error)
        ErrorActivity.reportError(this, if (error != null) listOf(error) else emptyList(), null, null, errorInfo)
    }

    protected fun postErrorResult(title: String?, text: String?) {
        var text = text
        disposeAll()
        stopForeground(true)
        stopSelf()

        if (title == null) {
            return
        }

        text = text ?: ""
        notificationBuilder = NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
            .setSmallIcon(R.drawable.ic_newpipe_triangle_white)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentTitle(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentText(text)
        notificationManager.notify(getNotificationId(), notificationBuilder.build())
    }

    protected fun createNotification(): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
            .setOngoing(true)
            .setProgress(-1, -1, true)
            .setSmallIcon(R.drawable.ic_newpipe_triangle_white)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentTitle(getString(getTitle()))
    }

    protected fun showToast(@StringRes message: Int) {
        showToast(getString(message))
    }

    protected fun showToast(message: String) {
        if (toast != null) toast!!.cancel()

        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast!!.show()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Error handling
    ///////////////////////////////////////////////////////////////////////////

    protected fun handleError(@StringRes errorTitle: Int, error: Throwable) {
        var message = getErrorMessage(error)

        if (TextUtils.isEmpty(message)) {
            val errorClassName = error.javaClass.name
            message = getString(R.string.error_occurred_detail, errorClassName)
        }

        showToast(errorTitle)
        postErrorResult(getString(errorTitle), message)
    }

    protected fun getErrorMessage(error: Throwable): String? {
        var message: String? = null
        if (error is SubscriptionExtractor.InvalidSourceException) {
            message = getString(R.string.invalid_source)
        } else if (error is FileNotFoundException) {
            message = getString(R.string.invalid_file)
        } else if (error is IOException) {
            message = getString(R.string.network_error)
        }
        return message
    }

    companion object {

        ///////////////////////////////////////////////////////////////////////////
        // Notification Impl
        ///////////////////////////////////////////////////////////////////////////

        private const val NOTIFICATION_SAMPLING_PERIOD = 2500
    }
}
