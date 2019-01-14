package com.notfour.blhx.jni

import android.content.Context
import android.os.Environment
import android.util.Log

/**
 * Created with author.
 * Description:
 * Date: 2019-01-14
 * Time: 下午4:56
 */
object ScreenShotUtils {
    const val TAG = "ScreenShotUtils"
    var SCREENSHOT_DIR = "${Environment.getExternalStorageDirectory().absolutePath}/blhx/screenshot"
    val screenShot = ScreenShot()

    fun takeScreenshot(out: String) {
        val state = screenShot.takeScreenshot(out)
        if (state > 0) {
            Log.e(TAG, "take screenshot fail")
        }
    }
}