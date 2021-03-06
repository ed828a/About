package com.dew.ed828.aihuaPlayer.util

import android.util.Log
import android.util.LruCache
import com.dew.ed828.aihuaPlayer.about.BuildConfig.DEBUG
import org.schabi.newpipe.extractor.Info

/**
 *
 * Created by Edward on 12/2/2018.
 *
 */

class InfoCache private constructor()
{

    val size: Long
        get() = synchronized(lruCache) {
            return lruCache.size().toLong()
        }

    fun getFromKey(serviceId: Int, url: String): Info? {
        if (DEBUG) Log.d(TAG, "getFromKey() called with: serviceId = [$serviceId], url = [$url]")
        synchronized(lruCache) {
            return getInfo(keyOf(serviceId, url))
        }
    }

    fun putInfo(serviceId: Int, url: String, info: Info) {
        if (DEBUG) Log.d(TAG, "putInfo() called with: info = [$info]")

        val expirationMillis = ServiceHelper.getCacheExpirationMillis(info.serviceId)
        synchronized(lruCache) {
            val data = CacheData(info, expirationMillis)
            lruCache.put(keyOf(serviceId, url), data)
        }
    }

    fun removeInfo(serviceId: Int, url: String) {
        if (DEBUG) Log.d(TAG, "removeInfo() called with: serviceId = [$serviceId], url = [$url]")
        synchronized(lruCache) {
            lruCache.remove(keyOf(serviceId, url))
        }
    }

    fun clearCache() {
        if (DEBUG) Log.d(TAG, "clearCache() called")
        synchronized(lruCache) {
            lruCache.evictAll()
        }
    }

    fun trimCache() {
        if (DEBUG) Log.d(TAG, "trimCache() called")
        synchronized(lruCache) {
            removeStaleCache()
            lruCache.trimToSize(TRIM_CACHE_TO)
        }
    }

    private class CacheData (val info: Info, timeoutMillis: Long) {
        private val expireTimestamp: Long = System.currentTimeMillis() + timeoutMillis

        val isExpired: Boolean
            get() = System.currentTimeMillis() > expireTimestamp

    }

    companion object {

        private val TAG = InfoCache::class.java.simpleName

        val instance = InfoCache()

        private const val MAX_ITEMS_ON_CACHE = 60

        /**
         * Trim the cache to this size
         */
        private const val TRIM_CACHE_TO = 30

        private val lruCache = LruCache<String, CacheData>(MAX_ITEMS_ON_CACHE)

        private fun keyOf(serviceId: Int, url: String): String = serviceId.toString() + url


        private fun removeStaleCache() {
            for ((key, data) in InfoCache.lruCache.snapshot()) {
                if (data != null && data.isExpired) {
                    InfoCache.lruCache.remove(key)
                }
            }
        }

        private fun getInfo(key: String): Info? {
            val data = InfoCache.lruCache.get(key) ?: return null

            if (data.isExpired) {
                InfoCache.lruCache.remove(key)
                return null
            }

            return data.info
        }
    }
}