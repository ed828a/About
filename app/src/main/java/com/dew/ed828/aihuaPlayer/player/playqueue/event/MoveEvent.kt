package com.dew.ed828.aihuaPlayer.player.playqueue.event

/**
 *
 * Created by Edward on 12/4/2018.
 *
 * This event is sent when two streams swap place in the play queue
 */

class MoveEvent(val fromIndex: Int, val toIndex: Int) : PlayQueueEvent {

    override fun type(): PlayQueueEventType = PlayQueueEventType.MOVE

}