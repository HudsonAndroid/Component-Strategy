package com.hudson.hrouter_api

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hudson.hrouter_api.param.input.BundleManager
import com.hudson.hrouter_api.repository.HRouterRepository

/**
 * 路由工具
 *
 * 使用前请主动初始化[initAsync]，推荐在Application中初始化
 *
 * 注意：不同组件的路由不能配置相同的group。
 * Created by Hudson on 2022/6/1.
 */
object HRouter {
    private var path: String? = null
    private var group: String? = null
    private val repository = HRouterRepository()

    /**
     * 初始化
     */
    fun initAsync(context: Context){
        Thread{
            repository.initComponentGroupRepos(context)
        }.start()
    }

    fun build(path: String): BundleManager{
        checkPathValid(path)

        val group: String = path.substring(1, path.indexOf("/", 1))
        return build(path, group)
    }

    private fun checkPathValid(path: String) {
        require(path.isNotEmpty() && path.startsWith("/")) {
            "路由不符合要求，path=$path"
        }

        require(path.lastIndexOf("/") != 0) {
            "路由地址不能为/"
        }
    }


    fun build(path: String, group: String): BundleManager {
        checkPathValid(path)
        require(group.isNotEmpty() && !group.contains("/")){
            "路由Group不合法，Group=$group"
        }
        this.path = path
        this.group = group
        return BundleManager()
    }


    @Throws(
        RuntimeException::class,
        IllegalArgumentException::class,
        UnsupportedOperationException::class
    )
    internal fun navigation(context: Context, bundle: Bundle){
        if(group.isNullOrEmpty() || path.isNullOrEmpty()){
            throw IllegalArgumentException("路由信息不能为空，path=$path，group=$group")
        }
        val destination = repository.findDestination(group!!, path!!)
        if(destination != null){
            if(Activity::class.java.isAssignableFrom(destination)){
                val intent = Intent(context, destination)
                intent.putExtras(bundle)
                context.startActivity(intent)
            }else{
                throw UnsupportedOperationException("目前路由页面仅支持Activity类型")
            }
        }else{
            throw RuntimeException("没有找到该路由页面，path=$path")
        }
    }
}