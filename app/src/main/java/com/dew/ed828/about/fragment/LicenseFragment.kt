package com.dew.ed828.about.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import com.dew.ed828.about.R
import com.dew.ed828.about.model.License
import com.dew.ed828.about.model.SoftwareComponent
import com.dew.ed828.about.model.StandardLicenses
import com.dew.ed828.about.util.LicenseFragmentHelper
import kotlinx.android.synthetic.main.fragment_license.view.*
import kotlinx.android.synthetic.main.item_software_component.view.*
import java.util.*

/**
 *
 * Created by Edward on 11/29/2018.
 *
 * This simple [Fragment] subclass
 * contains the software licenses
 * Activities that contain this fragment must
 * Use the [LicenseFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class LicenseFragment : Fragment() {

    private lateinit var softwareComponents: Array<SoftwareComponent>
    private var mComponentForContextMenu: SoftwareComponent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        softwareComponents = arguments!!.getParcelableArray(ARG_COMPONENTS) as Array<SoftwareComponent>

        // Sort components by name
        Arrays.sort(softwareComponents) { o1, o2 -> o1.name!!.compareTo(o2.name!!) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_license, container, false)

        rootView.appReadLicenseLink.setOnClickListener {
            LicenseFragment.showLicense(it.context, StandardLicenses.GPL3)
        }

        for (component in softwareComponents) {
            val componentView = inflater.inflate(R.layout.item_software_component, container, false)

            componentView.softwareName.text = component.name
            componentView.copyright.text = context!!.getString(
                R.string.copyright,
                component.years,
                component.copyrightOwner,
                component.license?.abbreviation
            )

            componentView.tag = component
            componentView.setOnClickListener { v ->
                val context = v.context
                if (context != null) {
                    showLicense(context, component.license!!)
                }
            }
            rootView.softwareComponentsView.addView(componentView)
            registerForContextMenu(componentView)
        }

        return rootView
    }

    override fun onCreateContextMenu(menu: ContextMenu, view: View, menuInfo: ContextMenu.ContextMenuInfo) {
        val inflater = activity!!.menuInflater
        val component = view.tag as SoftwareComponent
        menu.setHeaderTitle(component.name)
        inflater.inflate(R.menu.software_component, menu)
        super.onCreateContextMenu(menu, view, menuInfo)
        mComponentForContextMenu = view.tag as SoftwareComponent
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        // item.getMenuInfo() is null so we use the tag of the view
        val component = mComponentForContextMenu ?: return false

        return when (item!!.itemId) {
            R.id.action_website -> {
                openWebsite(component.link!!)
                true
            }

            R.id.action_show_license -> {
                showLicense(context, component.license!!)
                false
            }

            else -> false
        }
    }

    private fun openWebsite(componentLink: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(componentLink))
        startActivity(browserIntent)
    }

    companion object {
        private const val ARG_COMPONENTS = "components"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @param softwareComponents array
         * @return A new instance of fragment LicenseFragment.
         */
//        @JvmStatic
        fun newInstance(softwareComponents: Array<SoftwareComponent>): LicenseFragment {
            val fragment = LicenseFragment()
            val bundle = Bundle()
            bundle.putParcelableArray(ARG_COMPONENTS, softwareComponents)
            fragment.arguments = bundle
            return fragment
        }

        /**
         * Shows a popup containing the license
         * @param context the context to use
         * @param license the license to show
         */
        fun showLicense(context: Context?, license: License) {
            LicenseFragmentHelper(context as Activity?).execute(license)
        }

    }
}
