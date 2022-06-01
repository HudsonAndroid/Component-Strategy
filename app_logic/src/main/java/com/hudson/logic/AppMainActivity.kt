package com.hudson.logic

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.hudson.hrouter.annotation.HRouter
import com.hudson.hrouter.annotation.Parameter
import com.hudson.hrouter_api.param.ParameterInjectorManager

@HRouter(path = "/logic/main", group = "logic")
class AppMainActivity : AppCompatActivity() {

    @Parameter
    @JvmField var greet: String? = null

    @Parameter
    @JvmField
    var count: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        ParameterInjectorManager.inject(this)

        guiTestEntry()

    }

    private fun guiTestEntry(){
        val btn = findViewById<Button>(R.id.btn_center)
        try {
            val clazz = Class.forName("com.hudson.logic.DebugPageActivity")
            btn.setOnClickListener {
                startActivity(Intent(this, clazz))
            }
        }catch (e: ClassNotFoundException){
            e.printStackTrace()
        }
    }
}