package com.dew.ed828.aihuaPlayer.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.View

/**
 * Created by Edward on 12/12/2018.
 */

abstract class BasePreferenceFragment : PreferenceFragmentCompat() {

    protected lateinit var defaultPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDivider(null)
        updateTitle()
    }

    override fun onResume() {
        super.onResume()
        updateTitle()
    }

    private fun updateTitle() {
        if (activity is AppCompatActivity) {
            val actionBar = (activity as AppCompatActivity).supportActionBar
            actionBar?.title = preferenceScreen.title
        }
    }

    companion object {
        protected val TAG = BasePreferenceFragment::javaClass.name + "@" + Integer.toHexString(hashCode())
    }
}
