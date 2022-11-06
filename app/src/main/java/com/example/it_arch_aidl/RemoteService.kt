package com.example.it_arch_aidl

import android.app.Service
import android.content.Intent
import android.os.IBinder

class RemoteService: Service() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
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
    }
}