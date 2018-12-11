package com.dew.ed828.aihuaPlayer.player.playqueue.event

/**
 *
 * Created by Edward on 12/4/2018.
 *
 * This event is sent when initialization.
 *
 */

class InitEvent : PlayQueueEvent {

    override fun type(): PlayQueueEventType = PlayQueueEventType.INIT

}
