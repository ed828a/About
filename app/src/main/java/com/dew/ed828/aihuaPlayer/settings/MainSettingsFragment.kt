package com.dew.ed828.aihuaPlayer.settings


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dew.ed828.aihuaPlayer.about.BuildConfig.DEBUG
import com.dew.ed828.aihuaPlayer.about.R



/**
 * A simple [Fragment] subclass.
 *
 */
class MainSettingsFragment : BasePreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.main_settings)

        if (!DEBUG) {
            val debug = findPreference(getString(R.string.debug_pref_screen_key))
            preferenceScreen.removePreference(debug)
        }
    }


}
