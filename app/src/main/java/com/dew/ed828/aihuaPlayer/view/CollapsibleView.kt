package com.dew.ed828.aihuaPlayer.view


import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.os.Parcelable
import android.support.annotation.IntDef
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.util.Log
import android.view.View

import android.widget.LinearLayout
import com.dew.ed828.aihuaPlayer.player.Helper.AnimationUtils

import icepick.Icepick
import icepick.State

/**
 * Created by Edward on 12/13/2018.
 *
 * A view that can be fully collapsed and expanded.
 */
class CollapsibleView : LinearLayout {

    @State
    @ViewMode
    internal var currentState = COLLAPSED
    private var readyToChangeState: Boolean = false

    private var targetHeight = -1
    private var currentAnimator: ValueAnimator? = null
    private val listeners = ArrayList<StateListener>()

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(COLLAPSED, EXPANDED)
    annotation class ViewMode

    /**
     * This method recalculates the height of this view so it **must** be called when
     * some child changes (e.g. add new views, change text).
     */
    fun ready() {

        Log.d(TAG, getDebugLogString("ready() called"))

        measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST), View.MeasureSpec.UNSPECIFIED)
        targetHeight = measuredHeight

        layoutParams.height = if (currentState == COLLAPSED) 0 else targetHeight
        requestLayout()
        broadcastState()

        readyToChangeState = true


        Log.d(TAG, getDebugLogString("ready() *after* measuring"))

    }

    fun collapse() {
        Log.d(TAG, getDebugLogString("collapse() called"))

        if (!readyToChangeState) return

        val height = height
        if (height == 0) {
            setCurrentState(COLLAPSED)
            return
        }

        if (currentAnimator != null && currentAnimator!!.isRunning) currentAnimator!!.cancel()
        currentAnimator = AnimationUtils.animateHeight(this, ANIMATION_DURATION.toLong(), 0)

        setCurrentState(COLLAPSED)
    }

    fun expand() {
        Log.d(TAG, getDebugLogString("expand() called"))

        if (!readyToChangeState) return

        val height = height
        if (height == this.targetHeight) {
            setCurrentState(EXPANDED)
            return
        }

        if (currentAnimator != null && currentAnimator!!.isRunning) currentAnimator!!.cancel()
        currentAnimator = AnimationUtils.animateHeight(this, ANIMATION_DURATION.toLong(), this.targetHeight)
        setCurrentState(EXPANDED)
    }

    fun switchState() {
        if (!readyToChangeState) return

        if (currentState == COLLAPSED) {
            expand()
        } else {
            collapse()
        }
    }

    @ViewMode
    fun getCurrentState(): Int {
        return currentState
    }

    fun setCurrentState(@ViewMode currentState: Int) {
        this.currentState = currentState
        broadcastState()
    }

    fun broadcastState() {
        for (listener in listeners) {
            listener.onStateChanged(currentState)
        }
    }

    /**
     * Add a listener which will be listening for changes in this view (i.e. collapsed or expanded).
     */
    fun addListener(listener: StateListener) {
        if (listeners.contains(listener)) {
            throw IllegalStateException("Trying to add the same listener multiple times")
        }

        listeners.add(listener)
    }

    /**
     * Remove a listener so it doesn't receive more state changes.
     */
    fun removeListener(listener: StateListener) {
        listeners.remove(listener)
    }

    /**
     * Simple interface used for listening state changes of the [CollapsibleView].
     */
    interface StateListener {
        /**
         * Called when the state changes.
         *
         * @param newState the state that the [CollapsibleView] transitioned to,<br></br>
         * it's an integer being either [.COLLAPSED] or [.EXPANDED]
         */
        fun onStateChanged(@ViewMode newState: Int)
    }

    ///////////////////////////////////////////////////////////////////////////
    // State Saving
    ///////////////////////////////////////////////////////////////////////////

    public override fun onSaveInstanceState(): Parcelable? {
        return Icepick.saveInstanceState(this, super.onSaveInstanceState())
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        super.onRestoreInstanceState(Icepick.restoreInstanceState(this, state))

        ready()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Internal
    ///////////////////////////////////////////////////////////////////////////

    fun getDebugLogString(description: String): String {
        return String.format("%-100s → %s",
            description, "readyToChangeState = [" + readyToChangeState + "], currentState = [" + currentState + "], targetHeight = [" + targetHeight + "]," +
                    " mW x mH = [" + measuredWidth + "x" + measuredHeight + "]" +
                    " W x H = [" + width + "x" + height + "]")
    }

    companion object {
        private val TAG = CollapsibleView::class.java.simpleName

        ///////////////////////////////////////////////////////////////////////////
        // Collapse/expand logic
        ///////////////////////////////////////////////////////////////////////////

        private const val ANIMATION_DURATION = 420
        const val COLLAPSED = 0
        const val EXPANDED = 1
    }
}
