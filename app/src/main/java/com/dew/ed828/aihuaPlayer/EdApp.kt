package com.dew.ed828.aihuaPlayer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.dew.ed828.aihuaPlayer.about.BuildConfig
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.report.acra.AcraReportSenderFactory
import com.dew.ed828.aihuaPlayer.report.activity.ErrorActivity
import com.dew.ed828.aihuaPlayer.report.model.ErrorInfo
import com.dew.ed828.aihuaPlayer.report.model.UserAction
import com.dew.ed828.aihuaPlayer.settings.SettingsActivity
import com.dew.ed828.aihuaPlayer.util.ExtractorHelper
import com.dew.ed828.aihuaPlayer.util.ImageDownloader
import com.dew.ed828.aihuaPlayer.util.Localization
import com.dew.ed828.aihuaPlayer.util.StateSaver
import com.nostra13.universalimageloader.cache.memory.impl.LRULimitedMemoryCache
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration

import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import io.reactivex.annotations.NonNull
import io.reactivex.exceptions.CompositeException
import io.reactivex.exceptions.MissingBackpressureException
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.functions.Consumer
import io.reactivex.plugins.RxJavaPlugins
import org.acra.ACRA
import org.acra.config.ACRAConfigurationException
import org.acra.config.ConfigurationBuilder
import org.acra.sender.ReportSenderFactory
import org.schabi.newpipe.extractor.Downloader
import org.schabi.newpipe.extractor.NewPipe
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketException

/**
 *
 * Created by Edward on 12/3/2018.
 *
 */

class EdApp: Application() {

    private var refWatcher: RefWatcher? = null

    protected open val downloader: Downloader
        get() = com.dew.ed828.aihuaPlayer.util.Downloader.init(null)

    protected open val isDisposedRxExceptionsReported: Boolean
        get() = false

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        // temporarily comment off
//        initACRA()
    }

    override fun onCreate() {
        super.onCreate()

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        refWatcher = installLeakCanary()

        // Initialize settings first because others inits can use its values
        SettingsActivity.initSettings(this)

        NewPipe.init(downloader,
            Localization.getPreferredExtractorLocal(this))
        StateSaver.init(this)
        initNotificationChannel()

        // Initialize image loader
        ImageLoader.getInstance().init(getImageLoaderConfigurations(10, 50))

        configureRxJavaErrorHandler()
    }

    private fun configureRxJavaErrorHandler() {
        // https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling
        RxJavaPlugins.setErrorHandler(object : Consumer<Throwable> {
            override fun accept(@NonNull throwAble: Throwable) {
                var throwable = throwAble
                Log.e(TAG, "RxJavaPlugins.ErrorHandler called with -> : " +
                        "throwable = [" + throwable.javaClass.name + "]")

                if (throwable is UndeliverableException) {
                    // As UndeliverableException is a wrapper, get the cause of it to get the "real" exception
                    throwable = throwable.cause!!
                }

                val errors: List<Throwable>
                if (throwable is CompositeException) {
                    errors = throwable.exceptions
                } else {
                    errors = listOf(throwable)
                }

                for (error in errors) {
                    if (isThrowableIgnored(error)) return
                    if (isThrowableCritical(error)) {
                        reportException(error)
                        return
                    }
                }

                // Out-of-lifecycle exceptions should only be reported if a debug user wishes so,
                // When exception is not reported, log it
                if (isDisposedRxExceptionsReported) {
                    reportException(throwable)
                } else {
                    Log.e(TAG, "RxJavaPlugin: Undeliverable Exception received: ", throwable)
                }
            }

            private fun isThrowableIgnored(@NonNull throwable: Throwable): Boolean {
                // Don't crash the application over a simple network problem
                return ExtractorHelper.hasAssignableCauseThrowable(throwable,
                    IOException::class.java, SocketException::class.java, // network api cancellation
                    InterruptedException::class.java, InterruptedIOException::class.java) // blocking code disposed
            }

            private fun isThrowableCritical(@NonNull throwable: Throwable): Boolean {
                // Though these exceptions cannot be ignored
                return ExtractorHelper.hasAssignableCauseThrowable(throwable,
                    NullPointerException::class.java, IllegalArgumentException::class.java, // bug in app
                    OnErrorNotImplementedException::class.java, MissingBackpressureException::class.java,
                    IllegalStateException::class.java) // bug in operator
            }

            private fun reportException(@NonNull throwable: Throwable) {
                // Throw uncaught exception that will trigger the report system
                Thread.currentThread().uncaughtExceptionHandler
                    .uncaughtException(Thread.currentThread(), throwable)
            }
        })
    }

    private fun getImageLoaderConfigurations(memoryCacheSizeMb: Int,
                                             diskCacheSizeMb: Int): ImageLoaderConfiguration {
        return ImageLoaderConfiguration.Builder(this)
            .memoryCache(LRULimitedMemoryCache(memoryCacheSizeMb * 1024 * 1024))
            .diskCacheSize(diskCacheSizeMb * 1024 * 1024)
            .imageDownloader(ImageDownloader(applicationContext))
            .build()
    }

    private fun initACRA() {
        try {
            val acraConfig = ConfigurationBuilder(this)
                .setReportSenderFactoryClasses(*reportSenderFactoryClasses as Array<Class<out ReportSenderFactory>>)
                .setBuildConfigClass(BuildConfig::class.java)
                .build()
            ACRA.init(this, acraConfig)

        } catch (ace: ACRAConfigurationException) {
            ace.printStackTrace()
            ErrorActivity.reportError(this,
                ace,
                null,
                null,
                ErrorInfo.make(
                    UserAction.SOMETHING_ELSE, "none",
                    "Could not initialize ACRA crash report", R.string.app_ui_crash))
        }

    }

    fun initNotificationChannel() {
        if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            return
        }

        val id = getString(R.string.notification_channel_id)
        val name = getString(R.string.notification_channel_name)
        val description = getString(R.string.notification_channel_description)

        // Keep this below DEFAULT to avoid making noise on every notification update
        val importance = NotificationManager.IMPORTANCE_LOW

        val mChannel = NotificationChannel(id, name, importance)
        mChannel.description = description

        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.createNotificationChannel(mChannel)
    }

    protected open fun installLeakCanary(): RefWatcher {
        return RefWatcher.DISABLED
    }



    companion object {
        protected val TAG = EdApp::class.java.toString()

        private val reportSenderFactoryClasses = arrayOf<Class<*>>(AcraReportSenderFactory::class.java)

        fun getRefWatcher(context: Context): RefWatcher? {
            val application = context.applicationContext as EdApp
            return application.refWatcher
        }
    }
}