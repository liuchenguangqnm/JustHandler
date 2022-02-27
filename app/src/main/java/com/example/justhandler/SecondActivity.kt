package com.example.justhandler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.justhandler.testBean.People
import com.sunshine.justhandler.JustHandler
import com.sunshine.justhandler.invoke.InvokeFun
import com.sunshine.justhandler.invoke.InvokeThreadType

class SecondActivity : AppCompatActivity() {
    private var people: People? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        people = People()
        people?.register()
    }

    override fun onResume() {
        super.onResume()
        JustHandler.getMsg(
            this, object : InvokeFun("100", InvokeThreadType.MAIN_THREAD) {
                override fun invoke(obj: Any?) {
                    Log.i("Activity01", "$obj=======${Thread.currentThread().name}")
                }
            })

        JustHandler.getMsg(
            this, object : InvokeFun("200", InvokeThreadType.MAIN_THREAD) {
                override fun invoke(obj: Any?) {
                    Log.i("Activity01", "$obj=======${Thread.currentThread().name}")
                }
            })

        JustHandler.getMsg(
            this, object : InvokeFun("100", InvokeThreadType.RANDOM_THREAD) {
                override fun invoke(obj: Any?) {
                    Log.i("Activity02", "$obj=======${Thread.currentThread().name}")
                }
            })

        JustHandler.getMsg(
            this, object : InvokeFun("200", InvokeThreadType.RANDOM_THREAD) {
                override fun invoke(obj: Any?) {
                    Log.i("Activity02", "$obj=======${Thread.currentThread().name}")
                }
            })
    }

    override fun onDestroy() {
        people?.onDestroyToMsgTag("100")
        super.onDestroy()
    }
}