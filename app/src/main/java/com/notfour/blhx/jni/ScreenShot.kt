package com.notfour.blhx.jni

/**
 * Created with author.
 * Description:
 * Date: 2019-01-14
 * Time: 下午4:09
 */
class ScreenShot {
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    external fun takeScreenshot(out: String): Int

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("png")
            System.loadLibrary("native-lib")
        }
    }
}