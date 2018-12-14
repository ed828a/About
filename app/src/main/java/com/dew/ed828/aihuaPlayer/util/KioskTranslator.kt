package com.dew.ed828.aihuaPlayer.util

import android.content.Context
import com.dew.ed828.aihuaPlayer.about.R

/**
 * Created by Edward on 12/13/2018.
 */
object KioskTranslator {
    fun getTranslatedKioskName(kioskId: String, context: Context): String =
        when (kioskId) {
            "Trending" -> context.getString(R.string.trending)
            "Top 50" -> context.getString(R.string.top_50)
            "New & hot" -> context.getString(R.string.new_and_hot)
            else -> kioskId
        }

    fun getKioskIcons(kioskId: String, context: Context): Int =
        when (kioskId) {
            "Trending" -> ThemeHelper.resolveResourceIdFromAttr(context, R.attr.ic_hot)
            "Top 50" -> ThemeHelper.resolveResourceIdFromAttr(context, R.attr.ic_hot)
            "New & hot" -> ThemeHelper.resolveResourceIdFromAttr(context, R.attr.ic_hot)
            else -> 0
        }

}
