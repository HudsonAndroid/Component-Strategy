package com.hudson.hrouter.annotation.bean

import com.hudson.hrouter.annotation.enum.PageType
import javax.lang.model.element.Element

/**
 * Created by Hudson on 2022/5/31.
 */
class RouteInfo(
    val pageType: PageType,
    var element: Element?,
    val clazz: Class<*>?,
    val path: String,
    var group: String
){
    constructor(pageType: PageType,
                clazz: Class<*>?,
                path: String,
                group: String):this(pageType, null, clazz, path, group)

    override fun toString(): String {
        return "RouteInfo: path=$path, group=$group"
    }
}