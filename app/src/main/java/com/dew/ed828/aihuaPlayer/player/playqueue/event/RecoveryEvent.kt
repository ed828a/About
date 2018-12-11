package com.dew.ed828.aihuaPlayer.player.playqueue.event

/**
 *
 * Created by Edward on 12/4/2018.
 *
 * This event is sent when recovery record is set on a stream
 */

class RecoveryEvent(val index: Int, val position: Long) : PlayQueueEvent {

    override fun type(): PlayQueueEventType = PlayQueueEventType.RECOVERY

}
