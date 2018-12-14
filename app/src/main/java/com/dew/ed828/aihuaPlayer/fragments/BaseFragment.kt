package com.dew.ed828.aihuaPlayer.fragments


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.dew.ed828.aihuaPlayer.EdApp
import com.nostra13.universalimageloader.core.ImageLoader
import icepick.Icepick
import icepick.State

/**
 *
 * Created by Edward on 12/12/2018.
 *
 */

abstract class BaseFragment : Fragment() {

    protected var activity: AppCompatActivity? = null

    //These values are used for controlling framgents when they are part of the frontpage, @State marks this variable as a part of InstanceState
    @State
    var useAsFrontPage = false
    protected var mIsVisibleToUser = false

    protected fun getFM(): FragmentManager? =
        if (parentFragment == null)
            fragmentManager
        else
            parentFragment!!.fragmentManager

    fun useAsFrontPage(value: Boolean) {
        useAsFrontPage = value
    }

    ///////////////////////////////////////////////////////////////////////////
    // Fragment's Lifecycle
    ///////////////////////////////////////////////////////////////////////////

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as AppCompatActivity?
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate() called with: savedInstanceState = [$savedInstanceState]")
        super.onCreate(savedInstanceState)
        Icepick.restoreInstanceState(this, savedInstanceState)
        if (savedInstanceState != null) onRestoreInstanceState(savedInstanceState)
    }


    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(rootView, savedInstanceState)

        Log.d(TAG, "onViewCreated() called with: rootView = [$rootView], savedInstanceState = [$savedInstanceState]")

        initViews(rootView, savedInstanceState)
        initListeners()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Icepick.saveInstanceState(this, outState)
    }

    protected open fun onRestoreInstanceState(savedInstanceState: Bundle) {}

    override fun onDestroy() {
        super.onDestroy()

        val refWatcher = EdApp.getRefWatcher(getActivity()!!)
        refWatcher?.watch(this)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        mIsVisibleToUser = isVisibleToUser
    }

    ///////////////////////////////////////////////////////////////////////////
    // Init
    ///////////////////////////////////////////////////////////////////////////

    protected open fun initViews(rootView: View, savedInstanceState: Bundle?) {}

    protected open fun initListeners() {}

    ///////////////////////////////////////////////////////////////////////////
    // Utils
    ///////////////////////////////////////////////////////////////////////////

    open fun setTitle(title: String) {
        Log.d(TAG, "setTitle() called with: title = [$title]")
        if ((!useAsFrontPage || mIsVisibleToUser) && activity != null && activity!!.supportActionBar != null) {
            activity!!.supportActionBar!!.title = title
        }
    }

    companion object {
        private const val TAG = "BaseFragment"
        val imageLoader = ImageLoader.getInstance()!!
    }
}
