package com.dew.ed828.aihuaPlayer.player

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.player.PopupVideoPlayer.Companion.ACTION_CLOSE

class PopupVideoPlayerActivity : ServicePlayerActivity() {

    override fun getTag(): String {
        return TAG
    }

    override fun getSupportActionTitle(): String {
        return resources.getString(R.string.title_activity_popup_player)
    }

    override fun getBindIntent(): Intent {
        return Intent(this, PopupVideoPlayer::class.java)
    }

    override fun startPlayerListener() {
        if (player != null && player is PopupVideoPlayer.VideoPlayerImpl) {
            (player as PopupVideoPlayer.VideoPlayerImpl).setActivityListener(this)
        }
    }

    override fun stopPlayerListener() {
        if (player != null && player is PopupVideoPlayer.VideoPlayerImpl) {
            (player as PopupVideoPlayer.VideoPlayerImpl).removeActivityListener(this)
        }
    }

    override fun getPlayerOptionMenuResource(): Int {
        return R.menu.menu_play_queue_popup
    }

    override fun onPlayerOptionSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_switch_background) {
            this.player!!.setRecovery()
            applicationContext.sendBroadcast(getPlayerShutdownIntent())
            applicationContext.startService(getSwitchIntent(BackgroundPlayer::class.java))
            return true
        }
        return false
    }

    override fun getPlayerShutdownIntent(): Intent {
        return Intent(ACTION_CLOSE)
    }

    companion object {

        private const val TAG = "PopupVideoPlayerActivity"
    }
}
