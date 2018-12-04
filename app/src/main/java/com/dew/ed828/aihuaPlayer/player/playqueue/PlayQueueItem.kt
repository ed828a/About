package com.dew.ed828.aihuaPlayer.player.playqueue

import com.dew.ed828.aihuaPlayer.util.ExtractorHelper
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamType
import java.io.Serializable


/**
 *
 * Created by Edward on 12/2/2018.
 *
 * Todo: should Serializable  be replaced by Parcelable ?
 */

class PlayQueueItem private constructor(
    name: String?, url: String?,
    val serviceId: Int, val duration: Long,
    thumbnailUrl: String?, uploader: String?,
    val streamType: StreamType
) : Serializable {

    val title: String
    val url: String
    val thumbnailUrl: String
    val uploader: String

    // Item States, keep external access out
    var recoveryPosition: Long = 0

    init {
        this.title = name ?: EMPTY_STRING
        this.url = url ?: EMPTY_STRING
        this.thumbnailUrl = thumbnailUrl ?: EMPTY_STRING
        this.uploader = uploader ?: EMPTY_STRING

        this.recoveryPosition = RECOVERY_UNSET
    }

    constructor(info: StreamInfo) : this(
        info.name, info.url,
        info.serviceId, info.duration,
        info.thumbnailUrl, info.uploaderName,
        info.streamType
    ) {

        if (info.startPosition > 0)
            recoveryPosition = info.startPosition * 1000
    }

    constructor(item: StreamInfoItem) : this(
        item.name, item.url, item.serviceId, item.duration,
        item.thumbnailUrl, item.uploaderName, item.streamType
    )


    var error: Throwable? = null

    fun getStream(): Single<StreamInfo> =
        ExtractorHelper.getStreamInfo(this.serviceId, this.url, false)
            .subscribeOn(Schedulers.io())
            .doOnError { throwable -> error = throwable }

    companion object {
        const val RECOVERY_UNSET = java.lang.Long.MIN_VALUE
        private const val EMPTY_STRING = ""
    }
}
