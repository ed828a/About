package com.dew.ed828.aihuaPlayer.util

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.support.v7.app.AlertDialog
import android.webkit.WebView
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.about.model.License
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.ref.WeakReference

/**
 *
 * Created by Edward on 11/29/2018.
 *
 */

class LicenseFragmentHelper(activity: Activity?,
                            val weakReference: WeakReference<Activity> = WeakReference<Activity>(activity)
): AsyncTask<Any, Void, Int>(){
    private var license: License? = null

    fun getActivity(): Activity? {
        val activity = weakReference.get()

        return if (activity != null && activity.isFinishing) null else activity
    }

    override fun doInBackground(vararg params: Any?): Int {
        license = params[0] as License
        return 1
    }

    override fun onPostExecute(result: Int?) {
        val activity = getActivity()
        activity?.let {locActivity ->
            license?.let {locLicense ->
                val webViewData = getFormattedLicense(locActivity, locLicense)
                val alert = AlertDialog.Builder(activity)
                alert.setTitle(license!!.name)

                val webView = WebView(activity)
                webView.loadData(webViewData, "text/html; charset=UTF-8", null)

                alert.setView(webView)
                alert.setNegativeButton(android.R.string.ok) { dialog, which -> dialog.dismiss() }
                alert.show()
            }
        }
    }

    companion object {

        /**
         * @param context the context to use
         * @param license the license
         * @return String which contains a HTML formatted license page styled according to the context's theme
         */
        fun getFormattedLicense(context: Context, license: License): String {

            val licenseContent = StringBuilder()
            val webViewData: String
            try {
                val inputData = BufferedReader(InputStreamReader(context.assets.open(license.fileName!!), "UTF-8"))
                var str: String? = inputData.readLine()
                while (str != null) {
                    licenseContent.append(str)
                    str = inputData.readLine()
                }
                inputData.close()

                // split the HTML file and insert the stylesheet into the HEAD of the file
                val insert = licenseContent.toString().split("</head>".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                webViewData = (insert[0] + "<style type=\"text/css\">"
                        + getLicenseStylesheet(context) + "</style></head>"
                        + insert[1])
            } catch (e: Exception) {
                throw NullPointerException("Failed to load license file: Reason: ${e.message}, --- " + getLicenseStylesheet(context))
            }

            return webViewData
        }

        /**
         *
         * @param context
         * @return String which is a CSS stylesheet according to the context's theme
         */
        private fun getLicenseStylesheet(context: Context): String {
            val isLightTheme = ThemeHelper.isLightThemeSelected(context)
            return ("body{padding:12px 15px;margin:0;background:#"
                    + getHexRGBColor(context, if (isLightTheme)
                R.color.light_license_background_color
            else
                R.color.dark_license_background_color)
                    + ";color:#"
                    + getHexRGBColor(context, if (isLightTheme)
                R.color.light_license_text_color
            else
                R.color.dark_license_text_color) + ";}"
                    + "a[href]{color:#"
                    + getHexRGBColor(context, if (isLightTheme)
                R.color.light_youtube_primary_color
            else
                R.color.dark_youtube_primary_color) + ";}"
                    + "pre{white-space: pre-wrap;}")
        }

        /**
         * Cast R.color to a hexadecimal color value
         * @param context the context to use
         * @param color the color number from R.color
         * @return a six characters long String with hexadecimal RGB values
         */
        private fun getHexRGBColor(context: Context, color: Int): String {
            return context.resources.getString(color).substring(3)
        }
    }

}