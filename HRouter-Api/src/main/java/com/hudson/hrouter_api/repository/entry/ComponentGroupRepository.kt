package com.hudson.hrouter_api.repository.entry

/**
 * 组件路由组Group仓库
 * 可以认为是整个组件的各个路由组仓库类的Map集合。
 * Created by Hudson on 2022/5/31.
 */
interface ComponentGroupRepository {

    fun getGroupRepository(): Map<String, Class<out GroupPathRepository>>
}