package com.hudson.component

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import com.hudson.hrouter_api.HRouter
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
        // todo 切换的时候，由于finish，把默认的任务栈结束了，而打开的页面又要重新启动任务栈，所以切换动画是task的切换动画.
//        finish()
        // 跳转到App主业务逻辑页面
        HRouter.build("/logic/main")
            .withString("greet", "来自壳的问候")
            .withInt("count", 666)
            .navigation(this)
    }
}