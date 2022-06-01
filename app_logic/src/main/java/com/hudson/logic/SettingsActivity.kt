package com.hudson.logic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hudson.hrouter.annotation.HRoute

@HRoute(path = "/logic/settings", group = "user")
class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }
}