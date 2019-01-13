package com.notfour.blhx.utils

import android.hardware.input.InputManager
import android.os.SystemClock
import android.view.InputEvent
import android.view.MotionEvent
import java.lang.reflect.Method

/**
 * Created with author.
 * Description:
 * Date: 2019/1/13
 * Time: 21:16
 */
object InputUtils {
    private val inputManager: InputManager
    private val injectInputEventMethod: Method
    val INJECT_INPUT_EVENT_MODE_ASYNC = 0
    val INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT = 1
    const val INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH = 2

    init {
        val inputManagerClass = Class.forName("android.hardware.input.InputManager")
        val instanceMethod = inputManagerClass.getDeclaredMethod("getInstance")
        instanceMethod.isAccessible = true
        inputManager = instanceMethod.invoke(null) as InputManager
        injectInputEventMethod =
                inputManagerClass.getDeclaredMethod("injectInputEvent", InputEvent::class.java, Int::class.java)
        injectInputEventMethod.isAccessible = true
    }

    /**
     * 发送输入事件
     * @see InputManager#injectInputEvent
     */
    fun sendEvent(event: InputEvent, mode: Int) {
        injectInputEventMethod.invoke(inputManager, event, mode)
    }

    /**
     * 触摸按下
     * @param index-手指序号，用于多点触控中标记多只手指，分别控制它们的移动
     * @param x,y-屏幕坐标
     */
    fun touchDown(index: Int, x: Int, y: Int) {
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis()
        val event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x.toFloat(), y.toFloat(), 0)
        sendEvent(event, INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH)
    }

    fun touchMove(index: Int, x: Int, y: Int) {

    }

    /**
     * 触摸抬起
     */
    fun touchUp(index: Int, x: Int, y: Int) {
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis()
        val event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x.toFloat(), y.toFloat(), 0)
        sendEvent(event, INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH)
    }

    /**
     * 点击事件
     */
    fun tap(x: Int, y: Int) {
        touchDown(0, x, y)
        touchUp(0, x, y)
    }
}