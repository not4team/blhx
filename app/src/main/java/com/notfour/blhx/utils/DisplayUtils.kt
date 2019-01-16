package com.notfour.blhx.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

/**
 * Created with author.
 * Description:
 * Date: 2019-01-16
 * Time: 下午2:19
 */
class DisplayUtils {
    companion object {
        fun getScreenSize(context: Context): Pair<Int, Int> {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getRealMetrics(displayMetrics)
            return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
        }
    }
}