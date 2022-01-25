package com.example.justhandler

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.View
import com.bumptech.glide.Glide
import com.sunshine.justhandler.JustHandler

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        Looper.myQueue().addIdleHandler {
            Glide.with(MyApplication.context!!)
                .load("https://www.2008php.com/09_Website_appreciate/2010-07-11/20100711232024.jpg")
                .into(findViewById(R.id.iv_img))
            false
        }

        Thread {
            try {
                while (true) {
                    JustHandler.sendMsg("100", "王德发100")
                    JustHandler.sendMsg("200", "王德发200")
                    Thread.sleep(30)
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }.start()

        findViewById<View>(R.id.iv_img).setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
        }
    }
}