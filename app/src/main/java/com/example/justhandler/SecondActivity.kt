package com.example.justhandler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.justhandler.justhandler.JustHandler
import com.example.justhandler.justhandler.invoke.InvokeFun
import com.example.justhandler.justhandler.invoke.InvokeThreadType

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
                    Log.i("haha01", "$obj=======${Thread.currentThread().name}")
                }
            })

        JustHandler.getMsg(
            this, object : InvokeFun("200", InvokeThreadType.MAIN_THREAD) {
                override fun invoke(obj: Any?) {
                    Log.i("haha01", "$obj=======${Thread.currentThread().name}")
                }
            })

        JustHandler.getMsg(
            this, object : InvokeFun("100", InvokeThreadType.RANDOM_THREAD) {
                override fun invoke(obj: Any?) {
                    Log.i("haha02", "$obj=======${Thread.currentThread().name}")
                }
            })

        JustHandler.getMsg(
            this, object : InvokeFun("200", InvokeThreadType.RANDOM_THREAD) {
                override fun invoke(obj: Any?) {
                    Log.i("haha02", "$obj=======${Thread.currentThread().name}")
                }
            })
    }

    override fun onDestroy() {
        people?.onDestroyToMsgTag("100")
        super.onDestroy()
    }
}