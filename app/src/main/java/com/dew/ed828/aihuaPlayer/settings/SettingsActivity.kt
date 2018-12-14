package com.dew.ed828.aihuaPlayer.settings

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import android.support.v4.app.Fragment
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.util.ThemeHelper

class SettingsActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {


    override fun onCreate(savedInstanceBundle: Bundle?) {
        setTheme(ThemeHelper.getSettingsThemeStyle(this))

        super.onCreate(savedInstanceBundle)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        if (savedInstanceBundle == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_holder, MainSettingsFragment())
                .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowTitleEnabled(true)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            if (supportFragmentManager.backStackEntryCount == 0) {
                finish()
            } else
                supportFragmentManager.popBackStack()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, preference: Preference): Boolean {
        val fragment = Fragment.instantiate(this, preference.fragment, preference.extras)
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.animator.custom_fade_in, R.animator.custom_fade_out, R.animator.custom_fade_in, R.animator.custom_fade_out)
            .replace(R.id.fragment_holder, fragment)
            .addToBackStack(null)
            .commit()
        return true
    }

    companion object {

        fun initSettings(context: Context) {
            EdPlayerSettings.initSettings(context)
        }
    }
}
