package com.dew.ed828.about.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.dew.ed828.about.R
import com.dew.ed828.about.fragment.AboutFragment
import com.dew.ed828.about.fragment.LicenseFragment
import com.dew.ed828.about.model.SoftwareComponent
import com.dew.ed828.about.model.StandardLicenses

/**
 *
 * Created by Edward on 11/29/2018.
 *
 */

class SectionsPagerAdapter(val context: Context, fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager){
    override fun getItem(position: Int): Fragment? {
        return when (position) {
            0 -> AboutFragment.newInstance()
            1 -> LicenseFragment.newInstance(SOFTWARE_COMPONENTS)
            else -> null
        }
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> context.getString(R.string.tab_about)
            1 -> context.getString(R.string.tab_licenses)
            else -> null
        }
    }

    companion object {

        /**
         * List of all software components
         */
        private val SOFTWARE_COMPONENTS = arrayOf(
            SoftwareComponent(
                "Giga Get",
                "2014",
                "Peter Cai",
                "https://github.com/PaperAirplane-Dev-Team/GigaGet",
                StandardLicenses.GPL2),

            SoftwareComponent("NewPipe Extractor",
                "2017",
                "Christian Schabesberger",
                "https://github.com/TeamNewPipe/NewPipeExtractor",
                StandardLicenses.GPL3),

            SoftwareComponent("Jsoup",
                "2017",
                "Jonathan Hedley",
                "https://github.com/jhy/jsoup",
                StandardLicenses.MIT),

            SoftwareComponent("Rhino",
                "2015",
                "Mozilla",
                "https://www.mozilla.org/rhino/",
                StandardLicenses.MPL2),

            SoftwareComponent("ACRA",
                "2013",
                "Kevin Gaudin",
                "http://www.acra.ch",
                StandardLicenses.APACHE2),

            SoftwareComponent("Universal Image Loader",
                "2011 - 2015",
                "Sergey Tarasevich",
                "https://github.com/nostra13/Android-Universal-Image-Loader",
                StandardLicenses.APACHE2),

            SoftwareComponent("CircleImageView",
                "2014 - 2017",
                "Henning Dodenhof",
                "https://github.com/hdodenhof/CircleImageView",
                StandardLicenses.APACHE2),

            SoftwareComponent("ParalaxScrollView",
                "2014",
                "Nir Hartmann",
                "https://github.com/nirhart/ParallaxScroll",
                StandardLicenses.MIT),

            SoftwareComponent("NoNonsense-FilePicker",
                "2016",
                "Jonas Kalderstam",
                "https://github.com/spacecowboy/NoNonsense-FilePicker",
                StandardLicenses.MPL2),

            SoftwareComponent("ExoPlayer",
                "2014-2017",
                "Google Inc",
                "https://github.com/google/ExoPlayer",
                StandardLicenses.APACHE2),

            SoftwareComponent("RxAndroid",
                "2015",
                "The RxAndroid authors",
                "https://github.com/ReactiveX/RxAndroid",
                StandardLicenses.APACHE2),

            SoftwareComponent("RxJava",
                "2016-present",
                "RxJava Contributors",
                "https://github.com/ReactiveX/RxJava",
                StandardLicenses.APACHE2),

            SoftwareComponent("RxBinding",
                "2015",
                "Jake Wharton",
                "https://github.com/JakeWharton/RxBinding",
                StandardLicenses.APACHE2)
        )
    }
}