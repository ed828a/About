package com.dew.ed828.aihuaPlayer.fragments

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager

/**
 *
 * Created by Edward on 12/7/2018.
 *
 *  Todo: this class should reside in utils or adapters
 */

/**
 * Recycler view scroll listener which calls the method [.onScrolledDown]
 * if the view is scrolled below the last item.
 */
abstract class OnScrollBelowItemsListener : RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (dy > 0) {
            var pastVisibleItems = 0
            val visibleItemCount: Int
            val totalItemCount: Int
            val layoutManager = recyclerView.layoutManager
            if (layoutManager != null){
                visibleItemCount = layoutManager.childCount
                totalItemCount = layoutManager.itemCount

                // Already covers the GridLayoutManager case
                when (layoutManager) {
                    is LinearLayoutManager -> pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                    is StaggeredGridLayoutManager -> {
                        val positions = layoutManager.findFirstVisibleItemPositions(null)
                        if (positions != null && positions.isNotEmpty()) pastVisibleItems = positions[0]
                    }
                }

                if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                    onScrolledDown(recyclerView)
                }
            }
        }
    }

    /**
     * Called when the recycler view is scrolled below the last item.
     *
     * @param recyclerView the recycler view
     */
    abstract fun onScrolledDown(recyclerView: RecyclerView)
}
