package com.dew.ed828.aihuaPlayer.infolist.holder

import android.view.ViewGroup
import android.widget.TextView
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.infolist.builder.InfoItemBuilder
import com.dew.ed828.aihuaPlayer.util.Localization
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.channel.ChannelInfoItem

/**
 * Created by Edward on 12/13/2018.
 */


class ChannelInfoItemHolder(infoItemBuilder: InfoItemBuilder, parent: ViewGroup) : ChannelMiniInfoItemHolder(infoItemBuilder, R.layout.list_channel_item, parent) {
    private val itemChannelDescriptionView: TextView = itemView.findViewById(R.id.itemChannelDescriptionView)

    override fun updateFromItem(infoItem: InfoItem) {
        super.updateFromItem(infoItem)

        if (infoItem !is ChannelInfoItem) return

        itemChannelDescriptionView.text = infoItem.description
    }

    override fun getDetailLine(item: ChannelInfoItem): String {
        var details = super.getDetailLine(item)

        if (item.streamCount >= 0) {
            val formattedVideoAmount = Localization.localizeStreamCount(itemBuilder.context,
                item.streamCount)

            if (!details.isEmpty()) {
                details += " • $formattedVideoAmount"
            } else {
                details = formattedVideoAmount
            }
        }
        return details
    }
}
