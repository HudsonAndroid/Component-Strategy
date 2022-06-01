package com.hudson.hrouter_api.repository.entry

import com.hudson.hrouter.annotation.bean.RouteInfo

/**
 * 单个组的路由仓库
 * 可以理解为组件中各个页面路由的Map集合
 */
interface GroupPathRepository {

    fun getPathRepository(): Map<String, RouteInfo>
}