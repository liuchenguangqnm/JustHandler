package com.example.justhandler

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.View
import com.bumptech.glide.Glide
import com.sunshine.justhandler.JustHandler
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    private val uiExecutor = Executors.newScheduledThreadPool(2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        Looper.myQueue().addIdleHandler {
            Glide.with(MyApplication.context!!)
                .load("https://t7.baidu.com/it/u=2318403788,3418888102&fm=193&f=GIF")
                .into(findViewById(R.id.iv_img))
            false
        }

        val mutableListOf = HashMap<Any, Any>()
        mutableListOf.put(0, "asdf")
        mutableListOf.put(1, 0)
        mutableListOf.put(10000, 100)
        uiExecutor.scheduleAtFixedRate({
            JustHandler.sendMsg("100", mutableListOf)
            // JustHandler.sendMsg("200", "王德发200")
        }, 0, 50, TimeUnit.MILLISECONDS)

        findViewById<View>(R.id.iv_img).setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
        }
    }
}