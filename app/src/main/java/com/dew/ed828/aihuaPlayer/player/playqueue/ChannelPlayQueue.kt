package com.dew.ed828.aihuaPlayer.player.playqueue

import com.dew.ed828.aihuaPlayer.util.ExtractorHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.schabi.newpipe.extractor.channel.ChannelInfo
import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem

/**
 * Created by Edward on 12/13/2018.
 */

class ChannelPlayQueue : AbstractInfoPlayQueue<ChannelInfo, ChannelInfoItem> {

    override val tag: String
        get() = "ChannelPlayQueue@${Integer.toHexString(hashCode())}"

    constructor(item: ChannelInfoItem) : super(item) {}

    constructor(info: ChannelInfo) : this(info.serviceId, info.url, info.nextPageUrl, info.relatedItems, 0) {}

    constructor(serviceId: Int,
                url: String,
                nextPageUrl: String,
                streams: List<StreamInfoItem>,
                index: Int) : super(serviceId, url, nextPageUrl, streams, index) { }

    override fun fetch() {
        if (this.isInitial) {
            ExtractorHelper.getChannelInfo(this.serviceId, this.baseUrl, false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(headListObserver)
        } else {
            ExtractorHelper.getMoreChannelItems(this.serviceId, this.baseUrl, this.nextUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(nextPageObserver)
        }
    }
}