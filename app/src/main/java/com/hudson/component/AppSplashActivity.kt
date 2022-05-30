package com.hudson.component

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hudson.logic.AppMainActivity
import java.util.*

@SuppressLint("CustomSplashScreen")
// android 12有支持对启动屏的设置
class AppSplashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timer().schedule(object : TimerTask(){
            override fun run() {
                launchMainLogicPage()
            }
        }, 500)
    }

    fun launchMainLogicPage(){
        finish()
        startActivity( Intent(this, AppMainActivity::class.java))
    }
}