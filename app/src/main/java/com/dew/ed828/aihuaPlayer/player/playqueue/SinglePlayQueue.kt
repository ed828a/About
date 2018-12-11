package com.dew.ed828.aihuaPlayer.player.playqueue

import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem

/**
 *
 * Created by Edward on 12/4/2018.
 *
 */

class SinglePlayQueue : PlayQueue {
    constructor(item: StreamInfoItem) : super(0, listOf<PlayQueueItem>(PlayQueueItem(item))) {}

    constructor(info: StreamInfo) : super(0, listOf<PlayQueueItem>(PlayQueueItem(info))) {}

    constructor(items: List<StreamInfoItem>, index: Int) : super(index, playQueueItemsOf(items)) {}


    companion object {
        fun playQueueItemsOf(items: List<StreamInfoItem>): List<PlayQueueItem> {
            val playQueueItems = ArrayList<PlayQueueItem>(items.size)
            for (item in items) {
                playQueueItems.add(PlayQueueItem(item))
            }
            return playQueueItems
        }
    }

    override val isComplete: Boolean = true

    override fun fetch() {}
}
