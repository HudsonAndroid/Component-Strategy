package com.hudson.hrouter.annotation.enums

/**
 * 页面类型
 *
 * 目前仅支持activity类型
 * Created by Hudson on 2022/5/31.
 */
enum class PageType {
    ACTIVITY {
        override fun typePkg() = "android.app.Activity"
    };

    abstract fun typePkg(): String
}