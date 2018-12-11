package com.dew.ed828.aihuaPlayer.player.playqueue.event

/**
 *
 * Created by Edward on 12/4/2018.
 *
 * This event is sent when a pending stream is removed from the play queue
 */

class RemoveEvent(val removeIndex: Int, val queueIndex: Int) : PlayQueueEvent {

    override fun type(): PlayQueueEventType = PlayQueueEventType.REMOVE

}
