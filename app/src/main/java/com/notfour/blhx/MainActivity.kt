package com.notfour.blhx

import android.Manifest
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import com.notfour.blhx.utils.CvUtils
import com.notfour.blhx.utils.ExeCommand
import com.notfour.blhx.utils.FloatWindowManager
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.schedulers.Schedulers


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
        FloatWindowManager.setFloatBallClickListener { view ->
            Log.e(TAG, "悬浮球状态 ${view.state}")
            if (view.state) {
                view.setText("停止")
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
                val rxPermissions = RxPermissions(this)
                rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribeOn(Schedulers.io())
                    .subscribe({ grant ->
                        if (grant) {
//                        ScreenShotUtils.takeScreenshot("${ScreenShotUtils.SCREENSHOT_DIR}/screen.png")
                            CvUtils.findImageMatchTemplate("/sdcard/cvtest/template1.png", 50, 0, 0, 0, 0, 0)
                        } else {
                            Log.e(TAG, "${Manifest.permission.WRITE_EXTERNAL_STORAGE} be denied")
                        }
                    }) { error ->
                        error.printStackTrace()
                    }
            }
            R.id.btn_stop -> {
                FloatWindowManager.dismissWindow()
            }
        }
    }
}
