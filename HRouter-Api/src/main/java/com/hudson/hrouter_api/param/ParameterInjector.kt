package com.hudson.hrouter_api.param

/**
 * 路由参数的注入器
 * Created by Hudson on 2022/5/31.
 */
interface ParameterInjector {
    /**
     * 注入，完成目标路由的参数依赖的注入
     */
    fun inject(targetObject: Any)
}