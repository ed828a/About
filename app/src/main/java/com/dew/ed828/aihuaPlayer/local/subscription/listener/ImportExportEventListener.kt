package com.dew.ed828.aihuaPlayer.local.subscription.listener

/**
 * Created by Edward on 12/13/2018.
 */

interface ImportExportEventListener {
    /**
     * Called when the size has been resolved.
     *
     * @param size how many items there are to import/export
     */
    fun onSizeReceived(size: Int)

    /**
     * Called everytime an item has been parsed/resolved.
     *
     * @param itemName the name of the subscription item
     */
    fun onItemCompleted(itemName: String)
}