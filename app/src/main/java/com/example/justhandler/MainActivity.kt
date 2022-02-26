package com.example.justhandler

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.bumptech.glide.Glide
import com.sunshine.justhandler.JustHandler
import java.lang.Exception
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val uiExecutor = Executors.newScheduledThreadPool(2)
    private val a = mutableListOf<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        a.add(0)
        a.add("asdf")
//        a.add(People())
//        a.add(Handler(Looper.getMainLooper()))
//        a.add(Exception())
    }

    override fun onResume() {
        super.onResume()
        Looper.myQueue().addIdleHandler {
            Glide.with(MyApplication.context!!)
                .load("https://t7.baidu.com/it/u=2318403788,3418888102&fm=193&f=GIF")
                .into(findViewById(R.id.iv_img))
            false
        }


        uiExecutor.scheduleAtFixedRate({
            JustHandler.sendMsg("100", a)
            // JustHandler.sendMsg("200", "王德发200")
        }, 0, 2000, TimeUnit.MILLISECONDS)

        findViewById<View>(R.id.iv_img).setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
        }
    }
}