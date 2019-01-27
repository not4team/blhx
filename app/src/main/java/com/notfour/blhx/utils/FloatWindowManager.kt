/*
 * Copyright (C) 2016 Facishare Technology Co., Ltd. All Rights Reserved.
 */
package com.notfour.blhx.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import com.notfour.blhx.R
import com.notfour.blhx.widget.FloatBall

/**
 * Description:
 *
 * @author zhaozp
 * @since 2016-10-17
 */

object FloatWindowManager {
    private val TAG = "FloatWindowManager"
    private var isWindowDismiss = true
    private var windowManager: WindowManager? = null
    private lateinit var mParams: WindowManager.LayoutParams
    private lateinit var floatView: FloatBall
    private var dialog: Dialog? = null
    private var mFloatBallClickListener: (view: FloatBall) -> Unit = {}

    init {

    }

    fun applyOrShowFloatWindow(context: Context) {
        if (checkPermission(context)) {
            showWindow(context)
        } else {
            applyPermission(context)
        }
    }

    private fun checkPermission(context: Context): Boolean {
        //6.0 版本之后由于 google 增加了对悬浮窗权限的管理，所以方式就统一了
        if (Build.VERSION.SDK_INT < 23) {
            if (RomUtils.checkIsMiuiRom()) {
                return miuiPermissionCheck(context)
            } else if (RomUtils.checkIsMeizuRom()) {
                return meizuPermissionCheck(context)
            } else if (RomUtils.checkIsHuaweiRom()) {
                return huaweiPermissionCheck(context)
            } else if (RomUtils.checkIs360Rom()) {
                return qikuPermissionCheck(context)
            } else if (RomUtils.checkIsOppoRom()) {
                return oppoROMPermissionCheck(context)
            }
        }
        return commonROMPermissionCheck(context)
    }

    private fun huaweiPermissionCheck(context: Context): Boolean {
        return HuaweiUtils.checkFloatWindowPermission(context)
    }

    private fun miuiPermissionCheck(context: Context): Boolean {
        return MiuiUtils.checkFloatWindowPermission(context)
    }

    private fun meizuPermissionCheck(context: Context): Boolean {
        return MeizuUtils.checkFloatWindowPermission(context)
    }

    private fun qikuPermissionCheck(context: Context): Boolean {
        return QikuUtils.checkFloatWindowPermission(context)
    }

    private fun oppoROMPermissionCheck(context: Context): Boolean {
        return OppoUtils.checkFloatWindowPermission(context)
    }

    private fun commonROMPermissionCheck(context: Context): Boolean {
        //最新发现魅族6.0的系统这种方式不好用，天杀的，只有你是奇葩，没办法，单独适配一下
        if (RomUtils.checkIsMeizuRom()) {
            return meizuPermissionCheck(context)
        } else {
            var result = true
            if (Build.VERSION.SDK_INT >= 23) {
                try {
                    val clazz = Settings::class.java
                    val canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context::class.java)
                    result = canDrawOverlays.invoke(null, context) as Boolean
                } catch (e: Exception) {
                    Log.e(TAG, Log.getStackTraceString(e))
                }

            }
            return result
        }
    }

    private fun applyPermission(context: Context) {
        if (Build.VERSION.SDK_INT < 23) {
            if (RomUtils.checkIsMiuiRom()) {
                miuiROMPermissionApply(context)
            } else if (RomUtils.checkIsMeizuRom()) {
                meizuROMPermissionApply(context)
            } else if (RomUtils.checkIsHuaweiRom()) {
                huaweiROMPermissionApply(context)
            } else if (RomUtils.checkIs360Rom()) {
                ROM360PermissionApply(context)
            } else if (RomUtils.checkIsOppoRom()) {
                oppoROMPermissionApply(context)
            }
        } else {
            commonROMPermissionApply(context)
        }
    }

    private fun ROM360PermissionApply(context: Context) {
        showConfirmDialog(context, object : OnConfirmResult {
            override fun confirmResult(confirm: Boolean) {
                if (confirm) {
                    QikuUtils.applyPermission(context)
                } else {
                    Log.e(TAG, "ROM:360, user manually refuse OVERLAY_PERMISSION")
                }
            }
        })
    }

    private fun huaweiROMPermissionApply(context: Context) {
        showConfirmDialog(context, object : OnConfirmResult {
            override fun confirmResult(confirm: Boolean) {
                if (confirm) {
                    HuaweiUtils.applyPermission(context)
                } else {
                    Log.e(TAG, "ROM:huawei, user manually refuse OVERLAY_PERMISSION")
                }
            }
        })
    }

    private fun meizuROMPermissionApply(context: Context) {
        showConfirmDialog(context, object : OnConfirmResult {
            override fun confirmResult(confirm: Boolean) {
                if (confirm) {
                    MeizuUtils.applyPermission(context)
                } else {
                    Log.e(TAG, "ROM:meizu, user manually refuse OVERLAY_PERMISSION")
                }
            }
        })
    }

    private fun miuiROMPermissionApply(context: Context) {
        showConfirmDialog(context, object : OnConfirmResult {
            override fun confirmResult(confirm: Boolean) {
                if (confirm) {
                    MiuiUtils.applyMiuiPermission(context)
                } else {
                    Log.e(TAG, "ROM:miui, user manually refuse OVERLAY_PERMISSION")
                }
            }
        })
    }

    private fun oppoROMPermissionApply(context: Context) {
        showConfirmDialog(context, object : OnConfirmResult {
            override fun confirmResult(confirm: Boolean) {
                if (confirm) {
                    OppoUtils.applyOppoPermission(context)
                } else {
                    Log.e(TAG, "ROM:miui, user manually refuse OVERLAY_PERMISSION")
                }
            }
        })
    }

    /**
     * 通用 rom 权限申请
     */
    private fun commonROMPermissionApply(context: Context) {
        //这里也一样，魅族系统需要单独适配
        if (RomUtils.checkIsMeizuRom()) {
            meizuROMPermissionApply(context)
        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                showConfirmDialog(context, object : OnConfirmResult {
                    override fun confirmResult(confirm: Boolean) {
                        if (confirm) {
                            try {
                                commonROMPermissionApplyInternal(context)
                            } catch (e: Exception) {
                                Log.e(TAG, Log.getStackTraceString(e))
                            }

                        } else {
                            Log.d(TAG, "user manually refuse OVERLAY_PERMISSION")
                            //需要做统计效果
                        }
                    }
                })
            }
        }
    }

    private fun showConfirmDialog(context: Context, result: OnConfirmResult) {
        showConfirmDialog(context, "您的手机没有授予悬浮窗权限，请开启后再试", result)
    }

    private fun showConfirmDialog(context: Context, message: String, result: OnConfirmResult) {
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
        }

        dialog = AlertDialog.Builder(context).setCancelable(true).setTitle("")
            .setMessage(message)
            .setPositiveButton(
                "现在去开启"
            ) { dialog, which ->
                result.confirmResult(true)
                dialog.dismiss()
            }.setNegativeButton(
                "暂不开启"
            ) { dialog, which ->
                result.confirmResult(false)
                dialog.dismiss()
            }.create()
        dialog!!.show()
    }

    private interface OnConfirmResult {
        fun confirmResult(confirm: Boolean)
    }

    private fun showWindow(context: Context) {
        if (!isWindowDismiss) {
            Log.e(TAG, "view is already added here")
            return
        }

        isWindowDismiss = false
        if (windowManager == null) {
            windowManager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        }

        val size = Point()
        windowManager?.defaultDisplay?.getSize(size)
        val screenWidth = size.x
        val screenHeight = size.y
        Log.e(TAG, "screenWidth:$screenWidth,screenHeight:$screenHeight")

        mParams = WindowManager.LayoutParams()
        mParams.packageName = context.packageName
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        mParams.flags = (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        val mType: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            mType = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }
        mParams.type = mType
        mParams.format = PixelFormat.RGBA_8888
        mParams.gravity = Gravity.LEFT or Gravity.TOP
        mParams.x = screenWidth - context.resources.getDimension(R.dimen.floatball_width).toInt() -
                context.resources.getDimension(R.dimen.activity_horizontal_margin).toInt()
        mParams.y = screenHeight shr 1

        floatView = FloatBall(context)
        floatView.setParams(mParams)
        floatView.setIsShowing(true)
        if (mFloatBallClickListener != null) {
            floatView.setClickListener(mFloatBallClickListener)
        }
        windowManager?.addView(floatView, mParams)
    }

    fun dismissWindow() {
        if (isWindowDismiss) {
            Log.e(TAG, "window can not be dismiss cause it has not been added")
            return
        }

        isWindowDismiss = true
        floatView.setIsShowing(false)
        if (windowManager != null && floatView != null) {
            windowManager?.removeViewImmediate(floatView)
        }
    }

    fun setText(text: String) {
        floatView.setText(text)
    }

    private fun dp2px(context: Context, dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    fun setFloatBallClickListener(mFloatBallClickListener: (view: FloatBall) -> Unit) {
        this.mFloatBallClickListener = mFloatBallClickListener
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun commonROMPermissionApplyInternal(context: Context) {
        val clazz = Settings::class.java
        val field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION")

        val intent = Intent(field.get(null).toString())
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.data = Uri.parse("package:" + context.packageName)
        context.startActivity(intent)
    }

}
