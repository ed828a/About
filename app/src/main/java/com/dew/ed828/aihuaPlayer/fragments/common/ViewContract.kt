package com.dew.ed828.aihuaPlayer.fragments.common

/**
 * Created by Edward on 12/12/2018.
 */

interface ViewContract<I> {
    fun showLoading()
    fun hideLoading()
    fun showEmptyState()
    fun showError(message: String, showRetryButton: Boolean)

    fun handleResult(result: I)
}
