package com.dew.ed828.aihuaPlayer

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.about.activity.AboutActivity
import com.dew.ed828.aihuaPlayer.report.activity.ErrorActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // testing
        errorActivityButton.setOnClickListener {
            val intent = Intent(this, ErrorActivity::class.java)
            startActivity(intent)
        }

        buttonAboutActivity.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }
    }
}
