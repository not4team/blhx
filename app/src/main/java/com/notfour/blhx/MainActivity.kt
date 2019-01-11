package com.notfour.blhx

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import com.notfour.blhx.utils.FloatWindowManager

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


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
