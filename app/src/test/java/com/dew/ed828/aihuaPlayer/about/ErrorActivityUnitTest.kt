package com.dew.ed828.aihuaPlayer.about

import android.app.Activity
import com.dew.ed828.aihuaPlayer.MainActivity
import com.dew.ed828.aihuaPlayer.report.activity.ErrorActivity
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ErrorActivityUnitTest {

    @Test
    fun getReturnActivity() {
        var returnActivity: Class<out Activity>? = ErrorActivity.getReturnActivity(MainActivity::class.java)
        assertEquals(MainActivity::class.java, returnActivity)

//        returnActivity = ErrorActivity.getReturnActivity(RouterActivity::class.java)
//        assertEquals(RouterActivity::class.java, returnActivity)

        returnActivity = ErrorActivity.getReturnActivity(null)
        assertNull(returnActivity)

        returnActivity = ErrorActivity.getReturnActivity(Int::class.java)
        assertEquals(MainActivity::class.java, returnActivity)

//        returnActivity = ErrorActivity.getReturnActivity(VideoDetailFragment::class.java)
//        assertEquals(MainActivity::class.java, returnActivity)
    }

}
