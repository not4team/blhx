package com.notfour.blhx.jni

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.Surface
import com.notfour.blhx.utils.ExeCommand
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Method

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
            ExeCommand().run("screencap -p ${out}", 1000)
//            screenshot(1920, 1080, out)
        }
    }

    fun screenshot(width: Int, height: Int, out: String) {
        val clazz = Class.forName("android.view.SurfaceControl")
        var method: Method?
        var bitmap: Bitmap?
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
            method =
                    clazz.getDeclaredMethod(
                        "screenshot",
                        Rect::class.java,
                        Int::class.java,
                        Int::class.java,
                        Int::class.java
                    )
            method.isAccessible = true
            val rect = Rect(0, 0, width, height)
            bitmap = method.invoke(null, rect, width, height, Surface.ROTATION_0) as Bitmap?
        } else {
            method = clazz.getDeclaredMethod("screenshot", Int::class.java, Int::class.java)
            method.isAccessible = true
            bitmap = method.invoke(null, width, height) as Bitmap?
        }
        saveBitmap(bitmap, out)
    }

    fun saveBitmap(bitmap: Bitmap?, fileName: String) {
        val file = File(fileName)
        if (file.exists()) {
            file.delete()
        } else {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        val fos = FileOutputStream(file)
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, fos)
    }
}