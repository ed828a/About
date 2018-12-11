package com.dew.ed828.aihuaPlayer.local.builder

import android.content.Context
import android.util.Log
import android.widget.ImageView
import com.dew.ed828.aihuaPlayer.database.LocalItem
import com.dew.ed828.aihuaPlayer.util.OnClickGesture
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader

/**
 *
 * Created by Edward on 12/10/2018.
 *
 */

class LocalItemBuilder(val context: Context?) {
    private val imageLoader = ImageLoader.getInstance()

    var onItemSelectedListener: OnClickGesture<LocalItem>? = null

    fun displayImage(url: String, view: ImageView, options: DisplayImageOptions) {
        Log.d(TAG, "displayImage(): url = $url, options: $options")
        imageLoader.displayImage(url, view, options)
    }

    companion object {
        private val TAG = LocalItemBuilder::class.java.toString()
    }
}
