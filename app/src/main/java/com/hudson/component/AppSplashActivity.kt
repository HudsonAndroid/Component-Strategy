package com.hudson.component

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hudson.hrouter_api.HRouter
import com.hudson.logic.AppMainActivity
import java.util.*

@SuppressLint("CustomSplashScreen")
// android 12有支持对启动屏的设置
class AppSplashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HRouter.initAsync(this)
        Timer().schedule(object : TimerTask(){
            override fun run() {
                launchMainLogicPage()
            }
        }, 500)
    }

    fun launchMainLogicPage(){
        finish()
        // 跳转到App主业务逻辑页面
        HRouter.build("/logic/main")
            .withString("greet", "来自壳的问候")
            .withInt("count", 666)
            .navigation(this)
    }
}