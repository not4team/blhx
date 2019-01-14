package com.notfour.blhx

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.Surface
import android.view.View
import android.widget.Button
import com.notfour.blhx.jni.ScreenShotUtils
import com.notfour.blhx.utils.ExeCommand
import com.notfour.blhx.utils.FloatWindowManager


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = "MainActivity"
    private lateinit var mBtnStart: Button
    private lateinit var mBtnStop: Button
    private val STATE_RESULT_CODE = "result_code"
    private val STATE_RESULT_DATA = "result_data"
    private val REQUEST_MEDIA_PROJECTION = 1
    private var mResultCode: Int = 0
    private var mResultData: Intent? = null
    private var mMediaProjection: MediaProjection? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private lateinit var mMediaProjectionManager: MediaProjectionManager
    private var mSurface: Surface? = null
    private var mScreenDensity: Int = 0
    private var mScreenWidth: Int = 0
    private var mScreenHeight: Int = 0
    private var mImageReader: ImageReader? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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

        val metrics = DisplayMetrics()
        getWindowManager().getDefaultDisplay().getMetrics(metrics)
        mScreenDensity = metrics.densityDpi
        mScreenWidth = metrics.widthPixels
        mScreenHeight = metrics.heightPixels
        mMediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(
            mMediaProjectionManager.createScreenCaptureIntent(),
            REQUEST_MEDIA_PROJECTION
        )
        val exe = ExeCommand()
        exe.run("su", 1000)
        exe.run("chmod 777 /dev/graphics/fb0", 1000)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_start -> {
                FloatWindowManager.applyOrShowFloatWindow(this)
                ScreenShotUtils.takeScreenshot("${ScreenShotUtils.SCREENSHOT_DIR}/temp.png")
            }
            R.id.btn_stop -> {
                FloatWindowManager.dismissWindow()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK) {
                mResultCode = resultCode
                mResultData = data
                setUpMediaProjection()
                setUpVirtualDisplay()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setUpMediaProjection() {
        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 2);
        mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setUpVirtualDisplay() {
        mVirtualDisplay = mMediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            mScreenWidth, mScreenHeight, mScreenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mImageReader?.surface, null, null
        )
    }
}
