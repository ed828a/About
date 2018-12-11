package com.dew.ed828.aihuaPlayer.player.playqueue.event

/**
 *
 * Created by Edward on 12/4/2018.
 *
 * This event is sent when the index is changed
 */

class SelectEvent(val oldIndex: Int, val newIndex: Int) : PlayQueueEvent {

    override fun type(): PlayQueueEventType = PlayQueueEventType.SELECT

}
