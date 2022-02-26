package com.sunshine.justhandler.ipc

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.GuardedBy
import com.sunshine.justhandler.excutor.ThreadExecutor
import com.sunshine.justhandler.register.Register
import com.sunshine.justhandler.sender.MessageSender
import java.util.*

/**
 * created by: Sunshine at 2022/2/24
 * desc: 进程间通信方法集合
 */
internal class IPCFunction {
    companion object {
        private const val FILTER_NAME = "JustHandler"
        private const val INTENT_KEY = "data"
        private var application: Application? = null
        private var currentPackageName: String = ""
        private var currentProcessName: String = ""
        private val DEFAULT_IDE_PACKAGE = LinkedList<String>()

        init {
            // 必要数据初始化
            getIPCParams()
            if (currentPackageName.isNotEmpty())
                DEFAULT_IDE_PACKAGE.add(currentPackageName)
        }

        @Volatile
        private var isReceiving: Boolean = false
        private val boardCast: BroadcastReceiver by lazy {
            object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val json = intent?.getStringExtra(INTENT_KEY) ?: return
                    ThreadExecutor.execute {
                        IPCParser.getData(json, currentProcessName) { msgTag, msgData, post ->
                            MessageSender.sendMessageInner(msgTag, msgData, post)
                        }
                    }
                }
            }
        }
        private val intentFilter: IntentFilter by lazy {
            val filter = IntentFilter(FILTER_NAME)
            // filter.addCategory()
            filter
        }

        @GuardedBy("IPCFunction.class")
        fun ideRegister() {
            if (application == null) return
            if (Register.invokeWrappers.isEmpty() && Register.threadInvokeWrappers.isEmpty())
                return
            if (isReceiving) return
            isReceiving = true
            application?.registerReceiver(boardCast, intentFilter)
        }

        @GuardedBy("IPCFunction.class")
        fun ideUnRegister() {
            if (application == null) return
            if (Register.invokeWrappers.isNotEmpty() || Register.threadInvokeWrappers.isNotEmpty())
                return
            if (!isReceiving) return
            isReceiving = false
            application?.unregisterReceiver(boardCast)
        }

        fun sendIPCMsg(msgTag: String, data: Any? = null, post: Long) {
            // 前置判断
            if (currentPackageName.isEmpty() || currentProcessName.isEmpty()) return
            ThreadExecutor.execute {
                // 获取通信包
                val json = IPCParser.getJson(currentProcessName, msgTag, data, post)
                // 获取通信包intent
                val intent = Intent(FILTER_NAME)
                // 广播发送
                for (index in 0 until DEFAULT_IDE_PACKAGE.size) {
                    intent.setPackage(DEFAULT_IDE_PACKAGE[index])
                    intent.putExtra(INTENT_KEY, json)
                    application?.sendBroadcast(intent)
                }
            }
        }

        @GuardedBy("IDEFunction.class")
        private fun getIPCParams() {
            if (application != null) return
            try {
                @SuppressLint("PrivateApi")
                val clazz = Class.forName("android.app.ActivityThread")
                val activityThread = clazz.getMethod("currentActivityThread").invoke(null)
                val packageName = clazz.getMethod("currentPackageName").invoke(null)
                val processName = clazz.getMethod("currentProcessName").invoke(null)
                val app = clazz?.getMethod("getApplication")?.invoke(activityThread)
                application = if (app is Application) app
                else null
                currentPackageName = if (packageName is String) packageName
                else ""
                currentProcessName = if (processName is String) processName
                else ""
            } catch (e: Exception) {
                application = null
                currentPackageName = ""
                currentProcessName = ""
            }
        }
    }
}