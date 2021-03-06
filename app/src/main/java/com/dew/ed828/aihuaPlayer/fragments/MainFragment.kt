package com.dew.ed828.aihuaPlayer.fragments


import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*

import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.report.activity.ErrorActivity
import com.dew.ed828.aihuaPlayer.report.model.ErrorInfo
import com.dew.ed828.aihuaPlayer.report.model.UserAction
import com.dew.ed828.aihuaPlayer.settings.tabs.Tab
import com.dew.ed828.aihuaPlayer.settings.tabs.TabsManager
import com.dew.ed828.aihuaPlayer.util.NavigationHelper
import com.dew.ed828.aihuaPlayer.util.ServiceHelper
import org.schabi.newpipe.extractor.exceptions.ExtractionException


/**
 *
 * Created by Edward on 12/12/2018.
 *
 */

class MainFragment : BaseFragment(), TabLayout.OnTabSelectedListener {
    private var viewPager: ViewPager? = null
    private var pagerAdapter: SelectedTabsPagerAdapter? = null
    private var tabLayout: TabLayout? = null

    private val tabsList = ArrayList<Tab>()
    private var tabsManager: TabsManager? = null

    private var hasTabsChanged = false

    ///////////////////////////////////////////////////////////////////////////
    // Fragment's LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate() called, savedInstanceState = $savedInstanceState")
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        tabsManager = TabsManager.getManager(activity as Context)
        tabsManager!!.setSavedTabsListener(object : TabsManager.SavedTabsChangeListener{
            override fun onTabsChanged() {
                Log.d(TAG, "TabsManager.SavedTabsChangeListener: onTabsChanged called, isResumed = $isResumed")
                if (isResumed) {
                    updateTabs()
                } else {
                    hasTabsChanged = true
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        super.initViews(rootView, savedInstanceState)

        tabLayout = rootView.findViewById(R.id.main_tab_layout)
        viewPager = rootView.findViewById(R.id.pager) as ViewPager

        /*  Nested fragment, use child fragment here to maintain backstack in view pager. */
        pagerAdapter = SelectedTabsPagerAdapter(childFragmentManager)
        viewPager!!.adapter = pagerAdapter

        tabLayout!!.setupWithViewPager(viewPager)
        tabLayout!!.addOnTabSelectedListener(this)
        updateTabs()
    }

    override fun onResume() {
        super.onResume()

        if (hasTabsChanged) {
            hasTabsChanged = false
            updateTabs()
        }

        Log.d(TAG, "MainFragment is onResume now.")
    }

    override fun onDestroy() {
        super.onDestroy()
        tabsManager!!.unsetSavedTabsListener()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Menu
    ///////////////////////////////////////////////////////////////////////////

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        Log.d(TAG, "onCreateOptionsMenu() called with: menu = [$menu], inflater = [$inflater]")
        inflater!!.inflate(R.menu.main_fragment_menu, menu)

        val supportActionBar = activity?.supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_search -> {
                try {

                    NavigationHelper.openSearchFragment(
                        fragmentManager,
                        ServiceHelper.getSelectedServiceId(activity!!),
                        "")
                } catch (e: Exception) {
                    val context = getActivity()
                    context?.let{
                        ErrorActivity.reportUiError(it as AppCompatActivity, e)
                    }
                }

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Tabs
    ///////////////////////////////////////////////////////////////////////////

    fun updateTabs() {
        tabsList.clear()
        tabsList.addAll(tabsManager!!.getTabs())
        pagerAdapter!!.notifyDataSetChanged()

        viewPager!!.offscreenPageLimit = pagerAdapter!!.count
        updateTabsIcon()
        updateCurrentTitle()
    }

    private fun updateTabsIcon() {
        for (i in tabsList.indices) {
            val tabToSet = tabLayout!!.getTabAt(i)
            tabToSet?.setIcon(tabsList[i].getTabIconRes(activity!!))
        }
    }

    private fun updateCurrentTitle() {
        setTitle(tabsList[viewPager!!.currentItem].getTabName(requireContext()))
    }

    override fun onTabSelected(selectedTab: TabLayout.Tab) {
        Log.d(TAG, "onTabSelected() called with: selectedTab = [$selectedTab]")
        updateCurrentTitle()
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {}

    override fun onTabReselected(tab: TabLayout.Tab) {
        Log.d(TAG, "onTabReselected() called with: tab = [$tab]")
        updateCurrentTitle()
    }

    inner class SelectedTabsPagerAdapter (fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

        override fun getItem(position: Int): Fragment? {
            val tab = tabsList[position]

            var throwable: Throwable? = null
            var fragment: Fragment? = null
            try {
                fragment = tab.fragment
            } catch (e: ExtractionException) {
                throwable = e
            }

            if (throwable != null) {
                val context = activity
                context?.let {
                    ErrorActivity.reportError(it as Context, throwable, it.javaClass, null,
                        ErrorInfo.make(UserAction.UI_ERROR, "none", "", R.string.app_ui_crash))
                }

                return BlankFragment()
            }

            if (fragment is BaseFragment) {
                fragment.useAsFrontPage(true)
            }

            return fragment
        }

        override fun getItemPosition(`object`: Any): Int {
            // Causes adapter to reload all Fragments when
            // notifyDataSetChanged is called
            return PagerAdapter.POSITION_NONE
        }

        override fun getCount(): Int {
            return tabsList.size
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            childFragmentManager
                .beginTransaction()
                .remove(`object` as Fragment)
                .commitNowAllowingStateLoss()
        }
    }

    companion object {
//        protected val TAG = "${this::class.java.simpleName}@${Integer.toHexString(BaseFragment.hashCode())}"
        private const val TAG = "MainFragment"
    }
}
