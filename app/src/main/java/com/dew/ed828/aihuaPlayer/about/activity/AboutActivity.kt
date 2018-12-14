package com.dew.ed828.aihuaPlayer.about.activity

import android.os.Bundle
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.about.adapter.SectionsPagerAdapter
import com.dew.ed828.aihuaPlayer.util.NavigationHelper
import com.dew.ed828.aihuaPlayer.util.ThemeHelper
import kotlinx.android.synthetic.main.activity_about.*
import java.lang.IllegalArgumentException

/**
 *
 * Created by Edward on 11/29/2018.
 *
 */

class AboutActivity : AppCompatActivity() {

        /**
         * The [android.support.v4.view.PagerAdapter] that will provide
         * fragments for each of the sections. We use a
         * [FragmentPagerAdapter] derivative, which will keep every
         * loaded fragment in memory. If this becomes too memory intensive, it
         * may be best to switch to a
         * [android.support.v4.app.FragmentStatePagerAdapter].
         */
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            ThemeHelper.setTheme(this)

            setContentView(R.layout.activity_about)

            setSupportActionBar(toolbar)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            // Create the adapter that will return a fragment for each of the three
            // primary sections of the activity.
            // The viewPager that will host the section contents.
            // Set up viewPager with the sections adapter.
            viewPager.adapter = SectionsPagerAdapter(this, supportFragmentManager)

            tabsLayout.setupWithViewPager(viewPager)
        }


        override fun onCreateOptionsMenu(menu: Menu): Boolean {
            // Inflate the menu; this adds items to the action bar if it is present.
            menuInflater.inflate(R.menu.menu_about, menu)
            return true
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {

            return when (item.itemId) {
                android.R.id.home -> {
                    finish()
                    true
                }

                R.id.actionSettings -> {
                    NavigationHelper.openSettings(this)
                    Toast.makeText(this, "you clicked ActionSettings.", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.actionShowDownloads ->{
                    NavigationHelper.openDownloads(this)
                    Toast.makeText(this, "you clicked ActionShowDownloads.", Toast.LENGTH_SHORT).show()
                    throw IllegalArgumentException("This is a testing ErrorActivity")
                    true
                }

                else -> super.onOptionsItemSelected(item)
            }
        }
}
