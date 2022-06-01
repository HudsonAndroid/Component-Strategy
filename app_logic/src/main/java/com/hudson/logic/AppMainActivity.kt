package com.hudson.logic

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.hudson.hrouter.annotation.HRoute
import com.hudson.hrouter.annotation.Parameter
import com.hudson.hrouter_api.HRouter
import com.hudson.hrouter_api.param.ParameterInjectorManager

@HRoute(path = "/logic/main", group = "logic")
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

        findViewById<TextView>(R.id.tv_greet).text = greet

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
            btn.setOnClickListener {
                jumpOtherNonRelatedComponentPage()
            }
        }
    }

    /**
     * 跳转到没有依赖关系的其他组件的页面
     */
    private fun jumpOtherNonRelatedComponentPage(){
        HRouter.build("/product/main")
            .navigation(this)
    }
}