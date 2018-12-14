package com.dew.ed828.aihuaPlayer.local.model

/**
 * Created by Edward on 12/13/2018.
 */

class SuggestionItem(val fromHistory: Boolean, val query: String?) {

    override fun toString(): String {
        return "[$fromHistory→$query]"
    }
}
