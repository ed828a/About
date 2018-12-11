package com.dew.ed828.aihuaPlayer.player.playqueue.event

/**
 *
 * Created by Edward on 12/4/2018.
 *
 * This event is sent when the item at index has caused an exception
 */

class ErrorEvent(val errorIndex: Int, val queueIndex: Int, val isSkippable: Boolean) : PlayQueueEvent {

    override fun type(): PlayQueueEventType = PlayQueueEventType.ERROR

}
