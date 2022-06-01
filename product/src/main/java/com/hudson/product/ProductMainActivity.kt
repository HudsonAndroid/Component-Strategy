package com.hudson.product

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hudson.hrouter.annotation.HRoute

@HRoute(path = "/product/main", "product")
class ProductMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_main)
    }
}