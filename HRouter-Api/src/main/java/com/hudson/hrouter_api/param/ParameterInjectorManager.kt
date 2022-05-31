package com.hudson.hrouter_api.param

import android.app.Activity
import android.util.LruCache

/**
 * 路由参数管理器
 * Created by Hudson on 2022/5/31.
 */
object ParameterInjectorManager {
    private const val InjectorClazzSuffix = "_ParameterInjector"

    private val lruCache = LruCache<String, ParameterInjector>(32)

    /**
     * 给Activity注入来自路由的参数
     */
    // 如果需要支持其他类型的页面，需要新增对应的方法
    fun inject(activity: Activity){
        findInjector(activity.javaClass)?.inject(activity)
    }

    private fun findInjector(clazz: Class<*>): ParameterInjector? {
        val className = clazz.name
        val cache = lruCache[className]
        if(cache == null){
            try {
                val injectorClazz = Class.forName("$className$InjectorClazzSuffix")
                val injector = injectorClazz.newInstance() as ParameterInjector
                lruCache.put(className, injector)
                return injector
            }catch (e: ClassNotFoundException){
                e.printStackTrace()
            }
        }
        return cache
    }
}