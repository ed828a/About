package com.dew.ed828.aihuaPlayer.player.playqueue.event

import java.io.Serializable

/**
 *
 * Created by Edward on 12/4/2018.
 *
 */

interface PlayQueueEvent: Serializable{

    fun type(): PlayQueueEventType
}