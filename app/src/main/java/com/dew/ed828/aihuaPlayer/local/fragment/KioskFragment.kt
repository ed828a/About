package com.dew.ed828.aihuaPlayer.local.fragment


import android.os.Bundle
import android.util.Log
import android.view.*
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.fragments.BaseListInfoFragment
import com.dew.ed828.aihuaPlayer.player.Helper.AnimationUtils.animateView
import com.dew.ed828.aihuaPlayer.report.model.UserAction
import com.dew.ed828.aihuaPlayer.util.ExtractorHelper
import com.dew.ed828.aihuaPlayer.util.KioskTranslator
import icepick.State
import io.reactivex.Single
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.kiosk.KioskInfo


class KioskFragment : BaseListInfoFragment<KioskInfo>() {

    @State
    var kioskId = ""

    lateinit var kioskTranslatedName: String

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        kioskTranslatedName = KioskTranslator.getTranslatedKioskName(kioskId, activity!!)
        name = kioskTranslatedName
        Log.d(TAG, "KioskFragment::onCreate(), kioskTranslatedName = $kioskTranslatedName")


        // saving and restoring Instance States are done by IcePick
//        if (savedInstanceState != null) {
//            val serviceIdInString = savedInstanceState.getString(Constants.KEY_SERVICE_ID)
//            serviceId = serviceIdInString?.toInt() ?: Constants.NO_SERVICE_ID
//            Log.d(TAG, "KioskFragment::onCreate(), serviceId = $serviceId")
//        }
//    }
//
//    override fun onSaveInstanceState(savedInstanceState: Bundle) {
//        super.onSaveInstanceState(savedInstanceState)
//        savedInstanceState.putString(Constants.KEY_SERVICE_ID, serviceId.toString())
//        Log.d(TAG, "KioskFragment::onSaveInstanceState(), serviceId = $serviceId")
//    }
//
//    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
//        super.onRestoreInstanceState(savedInstanceState)
//        if (savedInstanceState != null) {
//            val serviceIdInString = savedInstanceState.getString(Constants.KEY_SERVICE_ID)
//            serviceId = serviceIdInString?.toInt() ?: Constants.NO_SERVICE_ID
//            Log.d(TAG, "KioskFragment::onRestoreInstanceState, serviceId = $serviceId")
//        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (useAsFrontPage && isVisibleToUser && activity != null) {
            try {
                setTitle(kioskTranslatedName)
            } catch (e: Exception) {
                onUnrecoverableError(e, UserAction.UI_ERROR,
                    "none",
                    "none", R.string.app_ui_crash)
            }

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView() called, and infalted R.layout.fragment_kiosk")
        return inflater.inflate(R.layout.fragment_kiosk, container, false)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
    }

    ///////////////////////////////////////////////////////////////////////////
    // Menu
    ///////////////////////////////////////////////////////////////////////////

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        val supportActionBar = activity!!.supportActionBar
        if (supportActionBar != null && useAsFrontPage) {
            supportActionBar.setDisplayHomeAsUpEnabled(false)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Load and handle
    ///////////////////////////////////////////////////////////////////////////

    public override fun loadResult(forceReload: Boolean): Single<KioskInfo> {
        Log.d(TAG, "loadResult(): serviceId=$serviceId")
        return ExtractorHelper.getKioskInfo(serviceId,
            url,
            forceReload)
    }

    public override fun loadMoreItemsLogic(): Single<ListExtractor.InfoItemsPage<*>> {
        return ExtractorHelper.getMoreKioskItems(serviceId,
            url,
            currentNextPageUrl)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Contract
    ///////////////////////////////////////////////////////////////////////////

    override fun showLoading() {
        super.showLoading()
        animateView(itemsList!!, false, 100)
    }

    override fun handleResult(result: KioskInfo) {
        super.handleResult(result)

        name = kioskTranslatedName
        if (!useAsFrontPage) {
            setTitle(kioskTranslatedName)
        }

        if (!result.errors.isEmpty()) {
            showSnackBarError(result.errors,
                UserAction.REQUESTED_KIOSK,
                NewPipe.getNameOfService(result.serviceId), result.url, 0)
        }
    }

    override fun handleNextItems(result: ListExtractor.InfoItemsPage<*>) {
        super.handleNextItems(result)

        if (!result.errors.isEmpty()) {
            showSnackBarError(result.errors,
                UserAction.REQUESTED_PLAYLIST, NewPipe.getNameOfService(serviceId), "Get next page of: $url", 0)
        }
    }

    companion object {

        private const val TAG = "KioskFragment"

        @Throws(ExtractionException::class)
        @JvmOverloads
        fun getInstance(serviceId: Int,
                        kioskId: String = NewPipe.getService(serviceId)
                            .kioskList
                            .defaultKioskId): KioskFragment {
            Log.d(TAG, "KioskFragment::getInstance(), serviceId = $serviceId")
            val instance = KioskFragment()
            val service = NewPipe.getService(serviceId)
            val kioskLinkHandlerFactory = service.kioskList
                .getListLinkHandlerFactoryByType(kioskId)

            instance.setInitialData(serviceId,
                kioskLinkHandlerFactory.fromId(kioskId).url, kioskId)

            instance.kioskId = kioskId

            return instance
        }
    }
}
