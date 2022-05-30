package com.hudson.hrouter.annotation

@Target(AnnotationTarget.CLASS) // 作用在类上
@Retention(AnnotationRetention.SOURCE) // 编译期生效
annotation class HRouter(
    val path: String,
    val group: String = "" // 一般指定为组件名
)