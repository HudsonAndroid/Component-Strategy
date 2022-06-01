package com.hudson.hrouter.annotation.bean

import com.hudson.hrouter.annotation.enums.PageType
import javax.lang.model.element.TypeElement

/**
 * Created by Hudson on 2022/5/31.
 */
class RouteInfo(
    val pageType: PageType,
    val path: String,
    var group: String,
    var pageClazz: Class<*>? = null // 运行期用
){

    var pageElement: TypeElement? = null // 解析期用

    override fun toString(): String {
        return "RouteInfo: path=$path, group=$group"
    }
}