package com.hudson.hrouter.annotation

/**
 * 路由参数类型
 *
 * 注意：注解的字段必须是公有且可修改的
 * Created by Hudson on 2022/5/31.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class Parameter(
    val name: String = "" // 注解使用处没有填写name的值，默认是变量名
)
