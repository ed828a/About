package com.dew.ed828.aihuaPlayer.local.model

import java.io.Serializable


/**
 * Created by Edward on 12/13/2018.
 */

class StackItem(val serviceId: Int, val url: String, var title: String?) : Serializable {

    override fun toString(): String {
        return "${serviceId.toString()}:$url > $title"
    }
}