package com.dew.ed828.aihuaPlayer.player.playqueue.event

/**
 *
 * Created by Edward on 12/4/2018.
 *
 * This event is sent when more streams are added to the play queue
 */

class AppendEvent(val amount: Int) : PlayQueueEvent {

    override fun type(): PlayQueueEventType = PlayQueueEventType.APPEND

}
