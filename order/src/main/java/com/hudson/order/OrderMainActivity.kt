package com.hudson.order

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hudson.hrouter.annotation.HRoute

@HRoute(path = "/order/main")
class OrderMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_main)
    }
}