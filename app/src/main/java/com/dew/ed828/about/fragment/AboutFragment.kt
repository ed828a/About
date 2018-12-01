package com.dew.ed828.about.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.dew.ed828.about.R
import com.dew.ed828.about.util.AppVersion
import kotlinx.android.synthetic.main.fragment_about.view.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must
 * Use the [AboutFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_about, container, false)
        val context = container?.context

        rootView.githubLink.setOnClickListener { nv -> openWebsite(context!!.getString(R.string.github_url), context) }

        rootView.donationLink.setOnClickListener { v -> openWebsite(context!!.getString(R.string.donation_url), context) }

        rootView.websiteLink.setOnClickListener { nv -> openWebsite(context!!.getString(R.string.website_url), context) }

        rootView.privacyPolicyLink.setOnClickListener { v -> openWebsite(context!!.getString(R.string.privacy_policy_url), context) }

        rootView.appVersion.text = AppVersion

        return rootView
    }

    private fun openWebsite(url: String, context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    private fun openWebsiteInWebView(url: String, context: Context){
        val webView = WebView(context)

        val alert = AlertDialog.Builder(context)

        alert.setView(webView)
        alert.setNegativeButton(android.R.string.ok) { dialog, which -> dialog.dismiss() }
        alert.show()
    }
    companion object {
        @JvmStatic
        fun newInstance() = AboutFragment()
    }
}
