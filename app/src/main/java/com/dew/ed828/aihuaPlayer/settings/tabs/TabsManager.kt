package com.dew.ed828.aihuaPlayer.settings.tabs

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.preference.PreferenceManager
import android.widget.Toast
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.settings.helper.TabsJsonHelper

/**
 * Created by Edward on 12/13/2018.
 */

class TabsManager private constructor(private val context: Context) {
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val savedTabsKey: String = context.getString(R.string.saved_tabs_key)

    fun getTabs(): List<Tab> {
        val savedJson = sharedPreferences.getString(savedTabsKey, null)
        return try {
            TabsJsonHelper.getTabsFromJson(savedJson)
        } catch (e: TabsJsonHelper.InvalidJsonException) {
            Toast.makeText(context, R.string.saved_tabs_invalid_json, Toast.LENGTH_SHORT).show()
            getDefaultTabs()
        }

    }

    fun getDefaultTabs(): List<Tab> = TabsJsonHelper.FALLBACK_INITIAL_TABS_LIST

    private var savedTabsChangeListener: SavedTabsChangeListener? = null
    private var preferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    fun saveTabs(tabList: List<Tab>) {
        val jsonToSave = TabsJsonHelper.getJsonToSave(tabList)
        sharedPreferences.edit().putString(savedTabsKey, jsonToSave).apply()
    }

    fun resetTabs() {
        sharedPreferences.edit().remove(savedTabsKey).apply()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Listener
    ///////////////////////////////////////////////////////////////////////////

    interface SavedTabsChangeListener {
        fun onTabsChanged()
    }

    fun setSavedTabsListener(listener: SavedTabsChangeListener) {
        if (preferenceChangeListener != null) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        }
        savedTabsChangeListener = listener
        preferenceChangeListener = getPreferenceChangeListener()
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    fun unsetSavedTabsListener() {
        if (preferenceChangeListener != null) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        }
        preferenceChangeListener = null
        savedTabsChangeListener = null
    }

    private fun getPreferenceChangeListener(): SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == savedTabsKey) {
                if (savedTabsChangeListener != null) savedTabsChangeListener!!.onTabsChanged()
            }
        }


    companion object {

        fun getManager(context: Context): TabsManager {
            return TabsManager(context)
        }
    }

}







