package com.notfour.blhx.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Created with author.
 * Description:
 * Date: 2019-01-11
 * Time: 下午1:55
 */
class BlhxService : Service() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}