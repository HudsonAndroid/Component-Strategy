package com.hudson.hrouter.annotation

/**
 * 路由配置
 *
 * 格式：
 * path:  /AA/BB/CC
 * group: AA
 *
 * group的内容（如AA）可以与path的第一块域AA不同，
 * 但是不建议如此设置，且跳转的时候需要明确指定group
 */
@Target(AnnotationTarget.CLASS) // 作用在类上
@Retention(AnnotationRetention.SOURCE) // 编译期生效
annotation class HRouter(
    val path: String,
    val group: String = "" // 一般指定为组件名
)