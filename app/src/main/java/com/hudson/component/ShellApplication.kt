package com.hudson.component

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.hudson.hrouter_api.HRouter
import com.hudson.hrouter_api.param.ParameterInjectorManager
import java.lang.Exception

/**
 * Created by Hudson on 2022/6/1.
 */
class ShellApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        HRouter.initAsync(this)

        autoInjectActivityPage()
    }

    private fun autoInjectActivityPage(){
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks{
            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityResumed(activity: Activity) {

            }

            override fun onActivityStarted(activity: Activity) {

            }

            override fun onActivityDestroyed(activity: Activity) {

            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                injectActivity(activity)
            }
        })
    }

    private fun injectActivity(activity: Activity){
        try{
            ParameterInjectorManager.inject(activity)
        }catch (e: Exception){
//            e.printStackTrace() ignore, 对于那些没有注册路由的页面
        }
    }
}