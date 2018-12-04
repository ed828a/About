package com.dew.ed828.aihuaPlayer.report.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.dew.ed828.aihuaPlayer.MainActivity
import com.dew.ed828.aihuaPlayer.about.BuildConfig
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.report.model.ErrorInfo
import com.dew.ed828.aihuaPlayer.report.model.UserAction
import com.dew.ed828.aihuaPlayer.util.ActivityCommunicator
import com.dew.ed828.aihuaPlayer.util.ThemeHelper
import kotlinx.android.synthetic.main.activity_error.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.acra.ReportField
import org.acra.collector.CrashReportData
import org.json.JSONArray
import org.json.JSONObject
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

class ErrorActivity : AppCompatActivity() {

    private var errorList: Array<String>? = null
    private var errorInfo: ErrorInfo? = null
    private var returnActivity: Class<*>? = null
    private var currentTimeStamp: String? = null

    private val contentLangString: String?
        get() = PreferenceManager.getDefaultSharedPreferences(this)
            .getString(this.getString(R.string.content_country_key), "none")

    private val osString: String
        get() {
            val osBase = if (Build.VERSION.SDK_INT >= 23) Build.VERSION.BASE_OS else "Android"
            return (System.getProperty("os.name")
                    + " " + (if (osBase.isEmpty()) "Android" else osBase)
                    + " " + Build.VERSION.RELEASE
                    + " - " + Integer.toString(Build.VERSION.SDK_INT))
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeHelper.setTheme(this) // set to default theme
        setContentView(R.layout.activity_error)

        val intent = intent

        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        actionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setTitle(R.string.error_report_title)
            it.setDisplayShowTitleEnabled(true)
        }

        val activityCommunicator = ActivityCommunicator.communicator
        returnActivity = activityCommunicator.returnActivity
        errorInfo = intent.getParcelableExtra(TAG_ERROR_INFO)
        errorList = intent.getStringArrayExtra(TAG_ERROR_LIST)

        // important add guru meditation
        addGuruMeditaion()
        currentTimeStamp = getCurrentTimeStamp()

        errorReportButton.setOnClickListener { view: View ->
            val context = this
            AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.privacy_policy_title)
                .setMessage(R.string.start_accept_privacy_policy)
                .setCancelable(false)
                .setNeutralButton(R.string.read_privacy_policy) { dialog, which ->
                    // Todo: change to view open source policy
                    val webIntent = Intent(Intent.ACTION_VIEW,
                        Uri.parse(context.getString(R.string.privacy_policy_url))
                    )
                    context.startActivity(webIntent)
                }
                .setPositiveButton(R.string.accept) { dialog, which ->
                    val i = Intent(Intent.ACTION_SENDTO)
                    i.setData(Uri.parse("mailto:$ERROR_EMAIL_ADDRESS"))
                        .putExtra(Intent.EXTRA_SUBJECT, ERROR_EMAIL_SUBJECT)
                        .putExtra(Intent.EXTRA_TEXT, buildJson())

                    startActivity(Intent.createChooser(i, "Send Email"))
                }
                .setNegativeButton(R.string.decline) { dialog, which ->
                    // do nothing
                }
                .show()

        }

        // normal bug_report
        errorInfo?.let { buildInfo(it) }
        if (errorInfo != null && errorInfo!!.message != 0) {
            errorMessageView.setText(errorInfo!!.message)
        } else {
            errorMessageView.visibility = View.GONE
            messageWhatHappenedView.visibility = View.GONE
        }

        errorView.text = formErrorText(errorList)

        //print stack trace once again for debugging:
        errorList?.let {
            for (e in it) {
                Log.e(TAG, e)
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.error_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> goToReturnActivity()
            R.id.menu_item_share_error -> {
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.putExtra(Intent.EXTRA_TEXT, buildJson())
                intent.type = "text/plain"
                startActivity(Intent.createChooser(intent, getString(R.string.share_dialog_title)))
            }
        }

        return false
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        goToReturnActivity()
    }

    private fun addGuruMeditaion() {
        //just an easter egg
        val text = errorSorryView.text.toString() +
                "\n" + getString(R.string.guru_meditation)

        errorSorryView.text = text
    }

    fun getCurrentTimeStamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        return dateFormat.format(Date())
    }

    private fun buildJson(): String {
        val errorObject = JSONObject()

        try {
            errorInfo?.let {
                errorObject.put("user_action", getUserActionString(it.userAction))
                    .put("request", it.request)
                    .put("content_language", contentLangString)
                    .put("service", it.serviceName)
                    .put("package", packageName)
                    .put("version", BuildConfig.VERSION_NAME)
                    .put("os", osString)
                    .put("time", currentTimeStamp)
            }

            val exceptionArray = JSONArray()
            errorList?.let {
                for (e in it) {
                    exceptionArray.put(e)
                }
            }

            with(errorObject){
                put("exceptions", exceptionArray)
                errorCommentBox?.let {
                    put("user_comment", it.text.toString())
                }
            }

            return errorObject.toString(3)
        } catch (e: Throwable) {
            Log.e(TAG, "Error while erroring: Could not build json")
            e.printStackTrace()
        }

        return ""
    }

    private fun getUserActionString(userAction: UserAction?): String {
        return if (userAction == null) {
            "Your description is in another castle."
        } else {
            userAction.message!!
        }
    }

    private fun buildInfo(info: ErrorInfo) {

        errorInfoLabelsView.text = getString(R.string.info_labels).replace("\\n", "\n")

        val text = ("${getUserActionString(info.userAction)}\n" +
                "${info.request}\n" +
                "$contentLangString\n" +
                "${info.serviceName}\n" +
                "$currentTimeStamp\n" +
                "$packageName\n" +
                "${BuildConfig.VERSION_NAME}\n" +
                "$osString")

        errorInfosView.text = text
    }

    private fun formErrorText(errorList: Array<String>?): String {
        val text = StringBuilder()
        errorList?.let {
            for (e in it) {
                text.append("-------------------------------------\n").append(e)
            }
        }

        text.append("-------------------------------------")
        return text.toString()
    }

    private fun goToReturnActivity() {
        val checkedReturnActivity = getReturnActivity(returnActivity)
        if (checkedReturnActivity == null) {
            super.onBackPressed()
        } else {
            val intent = Intent(this, checkedReturnActivity)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            NavUtils.navigateUpTo(this, intent)
        }
    }


    companion object {
        // LOG TAGS
        val TAG: String = ErrorActivity::class.java.simpleName

        // BUNDLE TAGS
        const val TAG_ERROR_INFO = "tag_error_info"
        const val TAG_ERROR_LIST = "tag_error_list"

        const val ERROR_EMAIL_ADDRESS = "ed828a@gmail.com"
        const val ERROR_EMAIL_SUBJECT = "Crash Exception in Ed YouTube Player " + BuildConfig.VERSION_NAME

        fun reportUiError(activity: AppCompatActivity, el: Throwable) {
            reportError(
                activity, el, activity.javaClass, null,
                ErrorInfo.make(UserAction.UI_ERROR, "none", "", R.string.app_ui_crash)
            )
        }

        private fun startErrorActivity(returnActivity: Class<*>?, context: Context, errorInfo: ErrorInfo, el: List<Throwable>?) {
            val ac = ActivityCommunicator.communicator
            ac.returnActivity = returnActivity
            val intent = Intent(context, ErrorActivity::class.java)
            intent.putExtra(TAG_ERROR_INFO, errorInfo)
            intent.putExtra(
                TAG_ERROR_LIST,
                errorListToStringList(el!!)
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        fun reportError(context: Context, el: List<Throwable>?,
                        returnActivity: Class<*>?, rootView: View?, errorInfo: ErrorInfo
        ) {
            if (rootView != null) {
                Snackbar.make(rootView, R.string.error_snackbar_message, 3 * 1000)
                    .setActionTextColor(Color.YELLOW)
                    .setAction(R.string.error_snackbar_action) { v ->
                        startErrorActivity(
                            returnActivity,
                            context,
                            errorInfo,
                            el
                        )
                    }.show()
            } else {
                startErrorActivity(
                    returnActivity,
                    context,
                    errorInfo,
                    el
                )
            }
        }

        fun reportError(context: Context, e: Throwable?,
                        returnActivity: Class<*>?, rootView: View?, errorInfo: ErrorInfo) {
            var el: MutableList<Throwable>? = null

            if (e != null) {
                el = Vector()
                el.add(e)
            }
            reportError(
                context,
                el,
                returnActivity,
                rootView,
                errorInfo
            )
        }

        // async call
        fun reportError(handler: Handler, context: Context, el: List<Throwable>?,
                        returnActivity: Class<*>?, rootView: View?, errorInfo: ErrorInfo) {
            handler.post {
                reportError(
                    context,
                    el,
                    returnActivity,
                    rootView,
                    errorInfo
                )
            }
        }

        // async call
        fun reportError(handler: Handler, context: Context, e: Throwable?,
                        returnActivity: Class<*>?, rootView: View?, errorInfo: ErrorInfo) {

            var el: MutableList<Throwable>? = null
            if (e != null) {
                el = Vector()
                el.add(e)
            }
            reportError(
                handler,
                context,
                el,
                returnActivity,
                rootView,
                errorInfo
            )
        }

        fun reportError(context: Context, report: CrashReportData, errorInfo: ErrorInfo) {
            // get key first (don't ask about this solution)
            var key: ReportField? = null
            for (k in report.keys) {
                if (k.toString() == "STACK_TRACE") {
                    key = k
                }
            }

            report[key]?.let {
                val el = arrayOf(report[key]!!.toString())

                val intent = Intent(context, ErrorActivity::class.java)
                intent.putExtra(TAG_ERROR_INFO, errorInfo)
                intent.putExtra(TAG_ERROR_LIST, el)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }

        private fun getStackTrace(throwable: Throwable): String {
            val sw = StringWriter()
            val pw = PrintWriter(sw, true)
            throwable.printStackTrace(pw)
            return sw.buffer.toString()
        }

        // errorList to StringList
        private fun errorListToStringList(stackTraces: List<Throwable>): Array<String?> {
            val outProd = arrayOfNulls<String>(stackTraces.size)
            for (i in stackTraces.indices) {
                outProd[i] =
                        getStackTrace(stackTraces[i])
            }
            return outProd
        }

        /**
         * Get the checked activity.
         *
         * @param returnActivity the activity to return to
         * @return the casted return activity or null
         */
        fun getReturnActivity(returnActivity: Class<*>?): Class<out Activity>? {
            var checkedReturnActivity: Class<out Activity>? = null
            if (returnActivity != null) {
                checkedReturnActivity = if (Activity::class.java.isAssignableFrom(returnActivity)) {
                    returnActivity.asSubclass(Activity::class.java)
                } else {
                    MainActivity::class.java
                }
            }
            return checkedReturnActivity
        }
    }
}
