package com.dew.ed828.aihuaPlayer.local.fragment


import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.support.annotation.DrawableRes
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.preference.PreferenceManager
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.database.subscription.SubscriptionEntity
import com.dew.ed828.aihuaPlayer.fragments.BaseStateFragment
import com.dew.ed828.aihuaPlayer.infolist.adapter.InfoListAdapter
import com.dew.ed828.aihuaPlayer.local.subscription.helper.ImportConfirmationDialog
import com.dew.ed828.aihuaPlayer.local.subscription.helper.SubscriptionService
import com.dew.ed828.aihuaPlayer.local.subscription.service.SubscriptionsExportService
import com.dew.ed828.aihuaPlayer.local.subscription.service.SubscriptionsImportService
import com.dew.ed828.aihuaPlayer.local.subscription.service.SubscriptionsImportService.Companion.KEY_VALUE
import com.dew.ed828.aihuaPlayer.local.subscription.service.SubscriptionsImportService.Companion.PREVIOUS_EXPORT_MODE
import com.dew.ed828.aihuaPlayer.player.Helper.AnimationUtils.animateRotation
import com.dew.ed828.aihuaPlayer.player.Helper.AnimationUtils.animateView
import com.dew.ed828.aihuaPlayer.report.model.UserAction
import com.dew.ed828.aihuaPlayer.util.*
import com.dew.ed828.aihuaPlayer.view.CollapsibleView
import com.nononsenseapps.filepicker.AbstractFilePickerFragment.KEY_MODE
import com.nononsenseapps.filepicker.Utils
import icepick.State
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Edward on 12/12/2018.
 */

class SubscriptionFragment : BaseStateFragment<List<SubscriptionEntity>>(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var itemsList: RecyclerView? = null
    @State
    var itemsListState: Parcelable? = null
    private var infoListAdapter: InfoListAdapter? = null
    private var updateFlags = 0

    private var whatsNewItemListHeader: View? = null
    private var importExportListHeader: View? = null

    @State
    var importExportOptionsState: Parcelable? = null
    private var importExportOptions: CollapsibleView? = null

    private var disposables: CompositeDisposable? = CompositeDisposable()
    private var subscriptionService: SubscriptionService? = null

    protected val listLayoutManager: RecyclerView.LayoutManager
        get() = LinearLayoutManager(activity)

    protected val gridLayoutManager: RecyclerView.LayoutManager
        get() {
            val resources = activity!!.resources
            var width = resources.getDimensionPixelSize(R.dimen.video_item_grid_thumbnail_image_width)
            width += (24 * resources.displayMetrics.density).toInt()
            val spanCount = Math.floor(resources.displayMetrics.widthPixels / width.toDouble()).toInt()
            val lm = GridLayoutManager(activity, spanCount)
            lm.spanSizeLookup = infoListAdapter!!.getSpanSizeLookup(spanCount)
            return lm
        }

    ///////////////////////////////////////////////////////////////////////////
    // Subscriptions import/export
    ///////////////////////////////////////////////////////////////////////////

    private var subscriptionBroadcastReceiver: BroadcastReceiver? = null


    private val deleteObserver: Observer<List<SubscriptionEntity>>
        get() = object : Observer<List<SubscriptionEntity>> {
            override fun onSubscribe(d: Disposable) {
                disposables!!.add(d)
            }

            override fun onNext(subscriptionEntities: List<SubscriptionEntity>) {
                subscriptionService!!.subscriptionTable().delete(subscriptionEntities)
            }

            override fun onError(exception: Throwable) {
                this@SubscriptionFragment.onError(exception)
            }

            override fun onComplete() {}
        }

    private val subscriptionObserver: Observer<List<SubscriptionEntity>>
        get() = object : Observer<List<SubscriptionEntity>> {
            override fun onSubscribe(d: Disposable) {
                showLoading()
                disposables!!.add(d)
            }

            override fun onNext(subscriptions: List<SubscriptionEntity>) {
                handleResult(subscriptions)
            }

            override fun onError(exception: Throwable) {
                this@SubscriptionFragment.onError(exception)
            }

            override fun onComplete() {}
        }

    protected val isGridLayout: Boolean
        get() {
            val listMode = PreferenceManager.getDefaultSharedPreferences(activity).getString(getString(R.string.list_view_mode_key), getString(R.string.list_view_mode_value))
            return if ("auto" == listMode) {
                val configuration = resources.configuration
                configuration.orientation == Configuration.ORIENTATION_LANDSCAPE && configuration.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE)
            } else {
                "grid" == listMode
            }
        }

    ///////////////////////////////////////////////////////////////////////////
    // Fragment LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        PreferenceManager.getDefaultSharedPreferences(activity)
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (activity != null && isVisibleToUser) {
            setTitle(activity!!.getString(R.string.tab_subscriptions))
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity?.let {
            infoListAdapter = InfoListAdapter(it)
        }

        subscriptionService = SubscriptionService.getInstance(activity!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_subscription, container, false)
    }

    override fun onResume() {
        super.onResume()
        setupBroadcastReceiver()
        if (updateFlags != 0) {
            if (updateFlags and LIST_MODE_UPDATE_FLAG != 0) {
                val useGrid = isGridLayout
                itemsList!!.layoutManager = if (useGrid) gridLayoutManager else listLayoutManager
                infoListAdapter!!.setGridItemVariants(useGrid)
                infoListAdapter!!.notifyDataSetChanged()
            }
            updateFlags = 0
        }
    }

    override fun onPause() {
        super.onPause()
        itemsListState = itemsList!!.layoutManager!!.onSaveInstanceState()
        importExportOptionsState = importExportOptions!!.onSaveInstanceState()

        if (subscriptionBroadcastReceiver != null && activity != null) {
            LocalBroadcastManager.getInstance(activity!!).unregisterReceiver(subscriptionBroadcastReceiver!!)
        }
    }

    override fun onDestroyView() {
        if (disposables != null) disposables!!.clear()

        super.onDestroyView()
    }

    override fun onDestroy() {
        if (disposables != null) disposables!!.dispose()
        disposables = null
        subscriptionService = null

        PreferenceManager.getDefaultSharedPreferences(activity)
            .unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    //////////////////////////////////////////////////////////////////////////
    // Menu
    //////////////////////////////////////////////////////////////////////////

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        val supportActionBar = activity!!.supportActionBar
        if (supportActionBar != null) {
            supportActionBar.setDisplayShowTitleEnabled(true)
            setTitle(getString(R.string.tab_subscriptions))
        }
    }

    private fun setupBroadcastReceiver() {
        if (activity == null) return

        if (subscriptionBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(activity!!).unregisterReceiver(subscriptionBroadcastReceiver!!)
        }

        val filters = IntentFilter()
        filters.addAction(SubscriptionsExportService.EXPORT_COMPLETE_ACTION)
        filters.addAction(SubscriptionsImportService.IMPORT_COMPLETE_ACTION)
        subscriptionBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (importExportOptions != null) importExportOptions!!.collapse()
            }
        }

        LocalBroadcastManager.getInstance(activity!!).registerReceiver(subscriptionBroadcastReceiver!!, filters)
    }

    private fun addItemView(title: String, @DrawableRes icon: Int, container: ViewGroup): View {
        val itemRoot = View.inflate(context, R.layout.subscription_import_export_item, null)
        val titleView = itemRoot.findViewById<TextView>(android.R.id.text1)
        val iconView = itemRoot.findViewById<ImageView>(android.R.id.icon1)

        titleView.text = title
        iconView.setImageResource(icon)

        container.addView(itemRoot)
        return itemRoot
    }

    private fun setupImportFromItems(listHolder: ViewGroup) {
        val previousBackupItem = addItemView(getString(R.string.previous_export),
            ThemeHelper.resolveResourceIdFromAttr(context!!, R.attr.ic_backup), listHolder)
        previousBackupItem.setOnClickListener { item -> onImportPreviousSelected() }

        val iconColor = if (ThemeHelper.isLightThemeSelected(context!!)) Color.BLACK else Color.WHITE
        val services = resources.getStringArray(R.array.service_list)
        for (serviceName in services) {
            try {
                val service = NewPipe.getService(serviceName)

                val subscriptionExtractor = service.subscriptionExtractor ?: continue

                val supportedSources = subscriptionExtractor.supportedSources
                if (supportedSources.isEmpty()) continue

                val itemView = addItemView(serviceName, ServiceHelper.getIcon(service.serviceId), listHolder)
                val iconView = itemView.findViewById<ImageView>(android.R.id.icon1)
                iconView.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN)

                itemView.setOnClickListener { selectedItem -> onImportFromServiceSelected(service.serviceId) }
            } catch (e: ExtractionException) {
                throw RuntimeException("Services array contains an entry that it's not a valid service name ($serviceName)", e)
            }

        }
    }

    private fun setupExportToItems(listHolder: ViewGroup) {
        val previousBackupItem = addItemView(getString(R.string.file), ThemeHelper.resolveResourceIdFromAttr(context!!, R.attr.ic_save), listHolder)
        previousBackupItem.setOnClickListener { item -> onExportSelected() }
    }

    private fun onImportFromServiceSelected(serviceId: Int) {
        val fragmentManager = getFM()
        NavigationHelper.openSubscriptionsImportFragment(fragmentManager, serviceId)
    }

    private fun onImportPreviousSelected() {
        startActivityForResult(FilePickerActivityHelper.chooseSingleFile(activity!!), REQUEST_IMPORT_CODE)
    }

    private fun onExportSelected() {
        val date = SimpleDateFormat("yyyyMMddHHmm", Locale.ENGLISH).format(Date())
        val exportName = "newpipe_subscriptions_$date.json"
        val exportFile = File(Environment.getExternalStorageDirectory(), exportName)

        startActivityForResult(FilePickerActivityHelper.chooseFileToSave(activity!!, exportFile.absolutePath), REQUEST_EXPORT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && data.data != null && resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_EXPORT_CODE) {
                val exportFile = Utils.getFileForUri(data.data!!)
                if (!exportFile.parentFile.canWrite() || !exportFile.parentFile.canRead()) {
                    Toast.makeText(activity, R.string.invalid_directory, Toast.LENGTH_SHORT).show()
                } else {
                    activity!!.startService(Intent(activity, SubscriptionsExportService::class.java)
                        .putExtra(SubscriptionsExportService.KEY_FILE_PATH, exportFile.absolutePath))
                }
            } else if (requestCode == REQUEST_IMPORT_CODE) {
                val path = Utils.getFileForUri(data.data!!).absolutePath
                ImportConfirmationDialog.show(this, Intent(activity, SubscriptionsImportService::class.java)
                    .putExtra(KEY_MODE, PREVIOUS_EXPORT_MODE)
                    .putExtra(KEY_VALUE, path))
            }
        }
    }
    //////////////////////////////////////////////////////////////////////////
    // Fragment Views
    //////////////////////////////////////////////////////////////////////////

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        super.initViews(rootView, savedInstanceState)

        val useGrid = isGridLayout
        infoListAdapter = InfoListAdapter(getActivity()!!)
        itemsList = rootView.findViewById(R.id.items_list)
        itemsList!!.layoutManager = if (useGrid) gridLayoutManager else listLayoutManager

        val headerRootLayout: View = activity!!.layoutInflater.inflate(R.layout.subscription_header, itemsList, false)
        infoListAdapter!!.setHeader(headerRootLayout)
        whatsNewItemListHeader = headerRootLayout.findViewById(R.id.whats_new)
        importExportListHeader = headerRootLayout.findViewById(R.id.import_export)
        importExportOptions = headerRootLayout.findViewById(R.id.import_export_options)

        infoListAdapter!!.useMiniItemVariants(true)
        infoListAdapter!!.setGridItemVariants(useGrid)
        itemsList!!.adapter = infoListAdapter

        setupImportFromItems(headerRootLayout.findViewById(R.id.import_from_options))
        setupExportToItems(headerRootLayout.findViewById(R.id.export_to_options))

        if (importExportOptionsState != null) {
            importExportOptions!!.onRestoreInstanceState(importExportOptionsState!!)
            importExportOptionsState = null
        }

        importExportOptions!!.addListener(getExpandIconSyncListener(headerRootLayout.findViewById(R.id.import_export_expand_icon)))
        importExportOptions!!.ready()
    }

    private fun getExpandIconSyncListener(iconView: ImageView): CollapsibleView.StateListener {
        return object : CollapsibleView.StateListener {
            override fun onStateChanged(newState: Int) {
                animateRotation(iconView, 250, if (newState == CollapsibleView.COLLAPSED) 0 else 180)
            }
        }
    }

    override fun initListeners() {
        super.initListeners()

        infoListAdapter!!.setOnChannelSelectedListener(object : OnClickGesture<ChannelInfoItem>() {

            override fun selected(selectedItem: ChannelInfoItem) {
                val fragmentManager = getFM()
                NavigationHelper.openChannelFragment(fragmentManager,
                    selectedItem.serviceId,
                    selectedItem.url,
                    selectedItem.name)
            }

            override fun held(selectedItem: ChannelInfoItem) {
                showLongTapDialog(selectedItem)
            }

        })


        whatsNewItemListHeader!!.setOnClickListener { v ->
            val fragmentManager = getFM()
            NavigationHelper.openWhatsNewFragment(fragmentManager)
        }
        importExportListHeader!!.setOnClickListener { v -> importExportOptions!!.switchState() }
    }

    private fun showLongTapDialog(selectedItem: ChannelInfoItem) {
        val context = context
        val activity = getActivity()
        if (context == null || context.resources == null || getActivity() == null) return

        val commands = arrayOf(context.resources.getString(R.string.share), context.resources.getString(R.string.unsubscribe))

        val actions = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                0 -> shareChannel(selectedItem)
                1 -> deleteChannel(selectedItem)
                else -> {
                }
            }
        }

        val bannerView = View.inflate(activity, R.layout.dialog_title, null)
        bannerView.isSelected = true

        val titleView = bannerView.findViewById<TextView>(R.id.itemTitleView)
        titleView.text = selectedItem.name

        val detailsView = bannerView.findViewById<TextView>(R.id.itemAdditionalDetails)
        detailsView.visibility = View.GONE

        AlertDialog.Builder(activity)
            .setCustomTitle(bannerView)
            .setItems(commands, actions)
            .create()
            .show()

    }

    private fun shareChannel(selectedItem: ChannelInfoItem) {
        shareUrl(selectedItem.name, selectedItem.url)
    }

    @SuppressLint("CheckResult")
    private fun deleteChannel(selectedItem: ChannelInfoItem) {
        subscriptionService!!.subscriptionTable()
            .getSubscription(selectedItem.serviceId, selectedItem.url)
            .toObservable()
            .observeOn(Schedulers.io())
            .subscribe(deleteObserver)

        Toast.makeText(activity, getString(R.string.channel_unsubscribed), Toast.LENGTH_SHORT).show()
    }

    private fun resetFragment() {
        if (disposables != null) disposables!!.clear()
        if (infoListAdapter != null) infoListAdapter!!.clearStreamItemList()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Subscriptions Loader
    ///////////////////////////////////////////////////////////////////////////

    public override fun startLoading(forceLoad: Boolean) {
        super.startLoading(forceLoad)
        resetFragment()

        subscriptionService!!.subscription.toObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(subscriptionObserver)
    }

    override fun handleResult(result: List<SubscriptionEntity>) {
        super.handleResult(result)

        infoListAdapter!!.clearStreamItemList()

        if (result.isEmpty()) {
            whatsNewItemListHeader!!.visibility = View.GONE
            showEmptyState()
        } else {
            infoListAdapter!!.addInfoItemList(getSubscriptionItems(result))
            if (itemsListState != null) {
                itemsList!!.layoutManager!!.onRestoreInstanceState(itemsListState)
                itemsListState = null
            }
            whatsNewItemListHeader!!.visibility = View.VISIBLE
            hideLoading()
        }
    }


    private fun getSubscriptionItems(subscriptions: List<SubscriptionEntity>): List<InfoItem> {
        val items = ArrayList<InfoItem>()
        for (subscription in subscriptions) {
            items.add(subscription.toChannelInfoItem())
        }

        items.sortWith(Comparator { o1: InfoItem, o2: InfoItem -> o1.name.compareTo(o2.name, ignoreCase = true) })
        return items
    }

    ///////////////////////////////////////////////////////////////////////////
    // Contract
    ///////////////////////////////////////////////////////////////////////////

    override fun showLoading() {
        super.showLoading()
        animateView(itemsList!!, false, 100)
    }

    override fun hideLoading() {
        super.hideLoading()
        animateView(itemsList!!, true, 200)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Fragment Error Handling
    ///////////////////////////////////////////////////////////////////////////

    override fun onError(exception: Throwable): Boolean {
        resetFragment()
        if (super.onError(exception)) return true

        onUnrecoverableError(exception,
            UserAction.SOMETHING_ELSE,
            "none",
            "Subscriptions",
            R.string.general_error)
        return true
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == getString(R.string.list_view_mode_key)) {
            updateFlags = updateFlags or LIST_MODE_UPDATE_FLAG
        }
    }

    companion object {
        private const val REQUEST_EXPORT_CODE = 666
        private const val REQUEST_IMPORT_CODE = 667

        private const val LIST_MODE_UPDATE_FLAG = 0x32
    }
}
