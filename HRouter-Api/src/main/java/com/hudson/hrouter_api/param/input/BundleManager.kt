package com.hudson.hrouter_api.param.input

import android.content.Context
import android.os.Bundle
import com.hudson.hrouter_api.HRouter

/**
 * Created by Hudson on 2022/6/1.
 */
class BundleManager {
    private var bundle = Bundle()

    fun withString(key: String, value: String): BundleManager {
        bundle.putString(key, value)
        return this
    }

    fun withBoolean(key: String, value: Boolean): BundleManager {
        bundle.putBoolean(key, value)
        return this
    }

    fun withInt(key: String, value: Int): BundleManager {
        bundle.putInt(key, value)
        return this
    }

    fun withBundle(bundle: Bundle): BundleManager {
        this.bundle = bundle
        return this
    }

    // 跳转
    fun navigation(context: Context): Any {
        return HRouter.navigation(context, bundle)
    }
}