package com.notfour.blhx

import android.Manifest
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import com.notfour.blhx.jni.ScreenShotUtils
import com.notfour.blhx.utils.DisplayUtils
import com.notfour.blhx.utils.ExeCommand
import com.notfour.blhx.utils.FloatWindowManager
import com.tbruyelle.rxpermissions2.RxPermissions


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = "MainActivity"
    private lateinit var mBtnStart: Button
    private lateinit var mBtnStop: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mBtnStart = findViewById(R.id.btn_start)
        mBtnStop = findViewById(R.id.btn_stop)
        mBtnStart.setOnClickListener(this)
        mBtnStop.setOnClickListener(this)
        val (w, h) = DisplayUtils.getScreenSize(this)
        Log.w(TAG, "screen size is $w X $h")
        val rxPermissions = RxPermissions(this)
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe { grant ->
            Log.e(TAG, "request ${Manifest.permission.WRITE_EXTERNAL_STORAGE} has $grant")
        }
        FloatWindowManager.setFloatBallClickListener { view ->
            Log.e(TAG, "悬浮球状态 ${view.state}")
            if (view.state) {
                view.setText("停止")
                ScreenShotUtils.takeScreenshot("${ScreenShotUtils.SCREENSHOT_DIR}/screen.png")
            } else {
                view.setText("启动")
            }
            view.isEnabled = true
        }

        val exe = ExeCommand()
        exe.run("su", 1000)
        exe.run("chmod 777 /dev/graphics/fb0", 1000)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_start -> {
                FloatWindowManager.applyOrShowFloatWindow(this)
            }
            R.id.btn_stop -> {
                FloatWindowManager.dismissWindow()
            }
        }
    }
}
