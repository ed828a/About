package com.dew.ed828.aihuaPlayer.infolist.dialog

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.view.View
import android.widget.TextView
import com.dew.ed828.aihuaPlayer.about.R
import org.schabi.newpipe.extractor.stream.StreamInfoItem

/**
 * Created by Edward on 12/13/2018.
 */

class InfoItemDialog(activity: Activity,
                     commands: Array<String>,
                     actions: DialogInterface.OnClickListener,
                     title: String,
                     additionalDetail: String?) {

    private val dialog: AlertDialog

    init {

        val bannerView = View.inflate(activity, R.layout.dialog_title, null)
        bannerView.isSelected = true

        val titleView = bannerView.findViewById<TextView>(R.id.itemTitleView)
        titleView.text = title

        val detailsView = bannerView.findViewById<TextView>(R.id.itemAdditionalDetails)
        if (additionalDetail != null) {
            detailsView.text = additionalDetail
            detailsView.visibility = View.VISIBLE
        } else {
            detailsView.visibility = View.GONE
        }

        dialog = AlertDialog.Builder(activity)
            .setCustomTitle(bannerView)
            .setItems(commands, actions)
            .create()
    }

    constructor(activity: Activity,
                info: StreamInfoItem,
                commands: Array<String>,
                actions: DialogInterface.OnClickListener) : this(activity, commands, actions, info.name, info.uploaderName)


    fun show() {
        dialog.show()
    }
}
