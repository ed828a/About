package com.dew.ed828.aihuaPlayer.player.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.ActionBar
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.MediaController
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.database.stream.model.StreamEntity.Companion.STREAM_URL
import kotlinx.android.synthetic.main.activity_play_video.*


class PlayVideoActivity : AppCompatActivity() {

    private var videoUrl = ""

    private var actionBar: ActionBar? = null
    //    private var videoView: VideoView? = null
    private var position: Int = 0
    private var mediaController: MediaController? = null
    //    private var playVideoProgressBar: ProgressBar? = null
    private var decorView: View? = null
    private var uiIsHidden: Boolean = false
    private var isLandscape = true
    private var hasSoftKeys: Boolean = false

    private var prefs: SharedPreferences? = null

    private val navigationBarHeight: Int
        get() {
            val display = windowManager.defaultDisplay
            val realDisplayMetrics = DisplayMetrics()
            display.getRealMetrics(realDisplayMetrics)

            val displayMetrics = DisplayMetrics()
            display.getMetrics(displayMetrics)

            val realHeight = realDisplayMetrics.heightPixels
            val displayHeight = displayMetrics.heightPixels

            return realHeight - displayHeight
        }

    private val navigationBarWidth: Int
        get() {
            val display = windowManager.defaultDisplay
            val realDisplayMetrics = DisplayMetrics()
            display.getRealMetrics(realDisplayMetrics)

            val displayMetrics = DisplayMetrics()
            display.getMetrics(displayMetrics)

            val realWidth = realDisplayMetrics.widthPixels
            val displayWidth = displayMetrics.widthPixels

            return realWidth - displayWidth
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_video)

        volumeControlStream = AudioManager.STREAM_MUSIC

        //set background arrow style
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)

        isLandscape = checkIfLandscape()
        hasSoftKeys = checkIfHasSoftKeys()

        actionBar = supportActionBar
        assert(actionBar != null)
        actionBar!!.setDisplayHomeAsUpEnabled(true)

        val intent = intent
        if (mediaController == null) {
            //prevents back button hiding media controller controls (after showing them)
            //instead of exiting video
            //see http://stackoverflow.com/questions/6051825
            //also solves https://github.com/theScrabi/NewPipe/issues/99
            mediaController = object : MediaController(this) {
                override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                    val keyCode = event.keyCode
                    val uniqueDown = event.repeatCount == 0 && event.action == KeyEvent.ACTION_DOWN
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if (uniqueDown) {
                            if (isShowing) {
                                finish()
                            } else {
                                hide()
                            }
                        }
                        return true
                    }
                    return super.dispatchKeyEvent(event)
                }
            }
        }

        position = intent.getIntExtra(START_POSITION, 0) * 1000   //convert from seconds to milliseconds

        try {
            videoView.setMediaController(mediaController)
            videoView.setVideoURI(Uri.parse(intent.getStringExtra(STREAM_URL)))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        videoView.requestFocus()
        videoView.setOnPreparedListener {
            playVideoProgressBar.visibility = View.GONE
            videoView.seekTo(position)
            if (position <= 0) {
                videoView.start()
                showUi()
            } else {
                videoView.pause()
            }
        }
        videoUrl = intent.getStringExtra(VIDEO_URL)

        contentButton.setOnClickListener {
            if (uiIsHidden) {
                showUi()
            } else {
                hideUi()
            }
        }
        decorView = window.decorView
        decorView!!.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility == View.VISIBLE && uiIsHidden) {
                showUi()
            }
        }

        decorView!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)


        prefs = getPreferences(Context.MODE_PRIVATE)
        if (prefs!!.getBoolean(PREF_IS_LANDSCAPE, false) && !isLandscape) {
            toggleOrientation()
        }
    }

    override fun onCreatePanelMenu(featured: Int, menu: Menu): Boolean {
        super.onCreatePanelMenu(featured, menu)
        val inflater = menuInflater
        inflater.inflate(R.menu.video_player, menu)

        return true
    }

    public override fun onPause() {
        super.onPause()
        videoView.pause()
    }

    public override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        prefs = getPreferences(Context.MODE_PRIVATE)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            android.R.id.home -> finish()
            R.id.menu_item_share -> {
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.putExtra(Intent.EXTRA_TEXT, videoUrl)
                intent.type = "text/plain"
                startActivity(Intent.createChooser(intent, getString(R.string.share_dialog_title)))
            }
            R.id.menu_item_screen_rotation -> toggleOrientation()
            else -> {
                Log.e(TAG, "Error: MenuItem not known")
                return false
            }
        }
        return true
    }

    override fun onConfigurationChanged(config: Configuration) {
        super.onConfigurationChanged(config)

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isLandscape = true
            adjustMediaControlMetrics()
        } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            isLandscape = false
            adjustMediaControlMetrics()
        }
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putInt(POSITION, videoView.currentPosition)
        videoView.pause()
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        position = savedInstanceState.getInt(POSITION)
        videoView.seekTo(position)
    }

    private fun showUi() {
        try {
            uiIsHidden = false
            mediaController!!.show(100000)
            actionBar!!.show()
            adjustMediaControlMetrics()
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            val handler = Handler()
            handler.postDelayed({
                if (System.currentTimeMillis() - lastUiShowTime >= HIDING_DELAY) {
                    hideUi()
                }
            }, HIDING_DELAY)
            lastUiShowTime = System.currentTimeMillis()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun hideUi() {
        uiIsHidden = true
        actionBar!!.hide()
        mediaController!!.hide()
        decorView!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    private fun adjustMediaControlMetrics() {
        val mediaControllerLayout = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )

        if (!hasSoftKeys) {
            mediaControllerLayout.setMargins(20, 0, 20, 20)
        } else {
            val width = navigationBarWidth
            val height = navigationBarHeight
            mediaControllerLayout.setMargins(width + 20, 0, width + 20, height + 20)
        }
        mediaController!!.layoutParams = mediaControllerLayout
    }

    private fun checkIfHasSoftKeys(): Boolean {
        return navigationBarHeight != 0 || navigationBarWidth != 0
    }

    private fun checkIfLandscape(): Boolean {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels < displayMetrics.widthPixels
    }

    private fun toggleOrientation() {
        if (isLandscape) {
            isLandscape = false
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            isLandscape = true
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
        val editor = prefs!!.edit()
        editor.putBoolean(PREF_IS_LANDSCAPE, isLandscape)
        editor.apply()
    }

    companion object {

        // TODO: 11.09.15 add "choose stream" menu

        private val TAG = PlayVideoActivity::class.java.toString()
        const val VIDEO_URL = "video_url"
        const val STREAM_URL = "stream_url"
        const val VIDEO_TITLE = "video_title"
        private const val POSITION = "position"
        const val START_POSITION = "start_position"

        private const val HIDING_DELAY: Long = 3000
        private var lastUiShowTime: Long = 0
        private const val PREF_IS_LANDSCAPE = "is_landscape"
    }
}
