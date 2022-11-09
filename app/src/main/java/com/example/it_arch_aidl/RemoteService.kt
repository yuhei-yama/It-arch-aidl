package com.example.it_arch_aidl

import android.app.Service
import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.IBinder
import android.util.Log

class RemoteService: Service() {

    companion object{
        const val TAG = "RemoteService"
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "サービスが起動しました")
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG,"remoteサービスとバインドしました")
        return binder
    }

    override fun stopService(name: Intent?): Boolean {
        Log.d(TAG, "リモートサービスを停止しました")
        return super.stopService(name)
    }


    override fun onDestroy() {
        super.onDestroy()
    }


    private val binder = object : Secondary.Stub() {
        override fun getPid(): Int {
            return android.os.Process.myPid()
        }

        override fun basicTypes(
            anInt: Int,
            aLong: Long,
            aBoolean: Boolean,
            aFloat: Float,
            aDouble: Double,
            aString: String
        ) {
            // Does nothing
      }
        override fun served(input : String): String{
            return input+" やったぜ"
        }
    }
}