package com.dew.ed828.aihuaPlayer.player.playqueue.event

/**
 *
 * Created by Edward on 12/4/2018.
 *
 * This event is sent when queue is shuffled
 */

class ReorderEvent(val fromSelectedIndex: Int, val toSelectedIndex: Int) : PlayQueueEvent {

    override fun type(): PlayQueueEventType = PlayQueueEventType.REORDER

}
