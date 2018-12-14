package com.dew.ed828.aihuaPlayer.util

import android.content.Context
import android.preference.PreferenceManager
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.util.Log
import com.dew.ed828.aihuaPlayer.about.R
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.ServiceList.SoundCloud
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import java.util.concurrent.TimeUnit

/**
 *
 * Created by Edward on 12/2/2018.
 *
 */

object ServiceHelper {
    private val DEFAULT_FALLBACK_SERVICE = ServiceList.YouTube
    private const val TAG = "ServiceHelper"

    @DrawableRes
    fun getIcon(serviceId: Int): Int =
        when (serviceId) {
            0 -> R.drawable.place_holder_youtube
            1 -> R.drawable.place_holder_circle
            else -> R.drawable.service
        }


    fun getTranslatedFilterString(filter: String, context: Context): String =
        when (filter) {
            "all" -> context.getString(R.string.all)
            "videos" -> context.getString(R.string.videos)
            "channels" -> context.getString(R.string.channels)
            "playlists" -> context.getString(R.string.playlists)
            "tracks" -> context.getString(R.string.tracks)
            "users" -> context.getString(R.string.users)
            else -> filter
        }


    /**
     * Get a resource string with instructions for importing subscriptions for each service.
     *
     * @return the string resource containing the instructions or -1 if the service don't support it
     */
    @StringRes
    fun getImportInstructions(serviceId: Int): Int =
        when (serviceId) {
            0 -> R.string.import_youtube_instructions
            1 -> R.string.import_soundcloud_instructions
            else -> -1
        }


    /**
     * For services that support importing from a channel url, return a hint that will
     * be used in the EditText that the user will type in his channel url.
     *
     * @return the hint's string resource or -1 if the service don't support it
     */
    @StringRes
    fun getImportInstructionsHint(serviceId: Int): Int =
        when (serviceId) {
            1 -> R.string.import_soundcloud_instructions_hint
            else -> -1
        }

    fun getSelectedServiceId(context: Context): Int {

        val serviceName = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(context.getString(R.string.current_service_key), context.getString(R.string.default_service_value))

        val serviceId: Int = try {
            NewPipe.getService(serviceName).serviceId
        } catch (e: ExtractionException) {
            DEFAULT_FALLBACK_SERVICE.serviceId
        }

        Log.d(TAG, "the current Service Name: $serviceName, and the current ServiceId = $serviceId")

        return serviceId
    }

    fun setSelectedServiceId(context: Context, serviceId: Int) {
        val serviceName: String = try {
            NewPipe.getService(serviceId).serviceInfo.name
        } catch (e: ExtractionException) {
            DEFAULT_FALLBACK_SERVICE.serviceInfo.name
        }

        setSelectedServicePreferences(context, serviceName)
    }

    fun setSelectedServiceId(context: Context, serviceName: String) {
        var serviceName = serviceName
        val serviceId = NewPipe.getIdOfService(serviceName)
        if (serviceId == -1) serviceName = DEFAULT_FALLBACK_SERVICE.serviceInfo.name

        setSelectedServicePreferences(context, serviceName)
    }

    private fun setSelectedServicePreferences(context: Context, serviceName: String) =
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(context.getString(R.string.current_service_key), serviceName)
            .apply()


    fun getCacheExpirationMillis(serviceId: Int): Long =
        if (serviceId == SoundCloud.serviceId) {
            TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES)
        } else {
            TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)
        }


    fun isBeta(streamingService: StreamingService): Boolean =
        when (streamingService.serviceInfo.name) {
            "YouTube" -> false
            else -> true
        }

}