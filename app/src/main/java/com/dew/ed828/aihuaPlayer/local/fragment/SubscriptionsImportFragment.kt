package com.dew.ed828.aihuaPlayer.local.fragment


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.text.util.LinkifyCompat
import android.text.TextUtils
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.fragments.BaseFragment
import com.dew.ed828.aihuaPlayer.local.subscription.helper.ImportConfirmationDialog
import com.dew.ed828.aihuaPlayer.local.subscription.service.SubscriptionsImportService
import com.dew.ed828.aihuaPlayer.local.subscription.service.SubscriptionsImportService.Companion.CHANNEL_URL_MODE
import com.dew.ed828.aihuaPlayer.local.subscription.service.SubscriptionsImportService.Companion.INPUT_STREAM_MODE
import com.dew.ed828.aihuaPlayer.local.subscription.service.SubscriptionsImportService.Companion.KEY_MODE
import com.dew.ed828.aihuaPlayer.local.subscription.service.SubscriptionsImportService.Companion.KEY_VALUE
import com.dew.ed828.aihuaPlayer.report.activity.ErrorActivity
import com.dew.ed828.aihuaPlayer.report.model.ErrorInfo
import com.dew.ed828.aihuaPlayer.report.model.UserAction
import com.dew.ed828.aihuaPlayer.util.FilePickerActivityHelper
import com.dew.ed828.aihuaPlayer.util.KEY_SERVICE_ID
import com.dew.ed828.aihuaPlayer.util.NO_SERVICE_ID
import com.dew.ed828.aihuaPlayer.util.ServiceHelper
import com.nononsenseapps.filepicker.Utils
import icepick.State
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor.ContentSource.CHANNEL_URL


class SubscriptionsImportFragment : BaseFragment() {

    @State
    var currentServiceId = NO_SERVICE_ID

    private var supportedSources: List<SubscriptionExtractor.ContentSource>? = null
    private var relatedUrl: String? = null
    @StringRes
    private var instructionsString: Int = 0

    ///////////////////////////////////////////////////////////////////////////
    // Views
    ///////////////////////////////////////////////////////////////////////////

    private var infoTextView: TextView? = null

    private var inputText: EditText? = null
    private var inputButton: Button? = null

    fun setInitialData(serviceId: Int) {
        this.currentServiceId = serviceId
    }

    ///////////////////////////////////////////////////////////////////////////
    // Fragment LifeCycle
    ///////////////////////////////////////////////////////////////////////////


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupServiceVariables()
        if (supportedSources!!.isEmpty() && currentServiceId != NO_SERVICE_ID) {
            ErrorActivity.reportError(activity!!, emptyList(), null, null, ErrorInfo.make(
                UserAction.SOMETHING_ELSE,
                NewPipe.getNameOfService(currentServiceId), "Service don't support importing", R.string.general_error))
            activity!!.finish()
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            setTitle(getString(R.string.import_title))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_subscriptions_import, container, false)
    }

    //////////////////////////////////////////////////////////////////////////
    // Fragment Views
    //////////////////////////////////////////////////////////////////////////

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        super.initViews(rootView, savedInstanceState)

        inputButton = rootView.findViewById(R.id.input_button)
        inputText = rootView.findViewById(R.id.input_text)

        infoTextView = rootView.findViewById(R.id.info_text_view)

        // TODO: Support services that can import from more than one source (show the option to the user)
        if (supportedSources!!.contains(CHANNEL_URL)) {
            inputButton!!.setText(R.string.import_title)
            inputText!!.visibility = View.VISIBLE
            inputText!!.setHint(ServiceHelper.getImportInstructionsHint(currentServiceId))
        } else {
            inputButton!!.setText(R.string.import_file_title)
        }

        if (instructionsString != 0) {
            if (TextUtils.isEmpty(relatedUrl)) {
                setInfoText(getString(instructionsString))
            } else {
                setInfoText(getString(instructionsString, relatedUrl))
            }
        } else {
            setInfoText("")
        }

        val supportActionBar = activity!!.supportActionBar
        if (supportActionBar != null) {
            supportActionBar.setDisplayShowTitleEnabled(true)
            setTitle(getString(R.string.import_title))
        }
    }

    override fun initListeners() {
        super.initListeners()
        inputButton!!.setOnClickListener { v -> onImportClicked() }
    }

    private fun onImportClicked() {
        if (inputText!!.visibility == View.VISIBLE) {
            val value = inputText!!.text.toString()
            if (!value.isEmpty()) onImportUrl(value)
        } else {
            onImportFile()
        }
    }

    fun onImportUrl(value: String) {
        ImportConfirmationDialog.show(this, Intent(activity, SubscriptionsImportService::class.java)
            .putExtra(KEY_MODE, CHANNEL_URL_MODE)
            .putExtra(KEY_VALUE, value)
            .putExtra(KEY_SERVICE_ID, currentServiceId))
    }

    fun onImportFile() {
        startActivityForResult(FilePickerActivityHelper.chooseSingleFile(activity!!), REQUEST_IMPORT_FILE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) return

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_IMPORT_FILE_CODE && data.data != null) {
            val path = Utils.getFileForUri(data.data!!).absolutePath
            ImportConfirmationDialog.show(this, Intent(activity, SubscriptionsImportService::class.java)
                .putExtra(KEY_MODE, INPUT_STREAM_MODE)
                .putExtra(KEY_VALUE, path)
                .putExtra(KEY_SERVICE_ID, currentServiceId))
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Subscriptions
    ///////////////////////////////////////////////////////////////////////////

    private fun setupServiceVariables() {
        if (currentServiceId != NO_SERVICE_ID) {
            try {
                val extractor = NewPipe.getService(currentServiceId).subscriptionExtractor
                supportedSources = extractor.supportedSources
                relatedUrl = extractor.relatedUrl
                instructionsString = ServiceHelper.getImportInstructions(currentServiceId)
                return
            } catch (ignored: ExtractionException) {
            }

        }

        supportedSources = emptyList<SubscriptionExtractor.ContentSource>()
        relatedUrl = null
        instructionsString = 0
    }

    private fun setInfoText(infoString: String) {
        infoTextView!!.text = infoString
        LinkifyCompat.addLinks(infoTextView!!, Linkify.WEB_URLS)
    }

    companion object {
        private const val REQUEST_IMPORT_FILE_CODE = 666

        fun getInstance(serviceId: Int): SubscriptionsImportFragment {
            val instance = SubscriptionsImportFragment()
            instance.setInitialData(serviceId)
            return instance
        }
    }
}

