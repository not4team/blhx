package com.notfour.blhx.utils

/**
 * Created with author.
 * Description:
 * Date: 2019/1/13
 * Time: 21:06
 */
class CvUtils {
    companion object {
        /**
         * 模糊区域找图
         * @param picpath-将要寻找的图片文件名
         * @param degree-寻找精度，范围：1 ~ 100，当是100时为完全匹配
         * @param x1,y1-欲寻找的区域左上角顶点屏幕坐标
         * @param x2,y2-欲寻找的区域右下角顶点屏幕坐标
         * @param alpha-忽略的颜色值（透明色） 若无请填 0
         * @return 找到的图片的左上角顶点坐标，如未找到则返回 -1，-1
         */
        fun findImageInRegionFuzzy(
            picpath: String,
            degree: Int,
            x1: Int,
            y1: Int,
            x2: Int,
            y2: Int,
            alpha: Int
        ): IntArray {
            return intArrayOf()
        }
    }
}