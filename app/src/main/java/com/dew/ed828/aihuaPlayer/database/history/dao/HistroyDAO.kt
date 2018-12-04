package com.dew.ed828.aihuaPlayer.database.history.dao

import com.dew.ed828.aihuaPlayer.database.BasicDAO

/**
 *
 * Created by Edward on 12/4/2018.
 *
 */

interface HistoryDAO<T>: BasicDAO<T> {
    fun getLatestEntry(): T?
}