package com.notfour.blhx.utils

import com.notfour.blhx.jni.ScreenShotUtils
import org.opencv.core.*
import org.opencv.features2d.AKAZE
import org.opencv.features2d.DescriptorMatcher
import org.opencv.imgcodecs.Imgcodecs


/**
 * Created with author.
 * Description:
 * Date: 2019/1/13
 * Time: 21:06
 */
class CvUtils {
    companion object {

        init {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
        }

        const val TAG = "CvUtils"

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
            picPath: String,
            degree: Int,
            x1: Int,
            y1: Int,
            x2: Int,
            y2: Int,
            alpha: Int
        ): Pair<Int, Int> {
            val picMat = Imgcodecs.imread(picPath)
            val screenPath = "${ScreenShotUtils.SCREENSHOT_DIR}/screen.png"
            ScreenShotUtils.takeScreenshot(screenPath)
            val screenMat = Imgcodecs.imread(screenPath)
            val akaze = AKAZE.create()
            val kpts1 = MatOfKeyPoint()
            val kpts2 = MatOfKeyPoint()
            val desc1 = Mat()
            val desc2 = Mat()
            akaze.detectAndCompute(picMat, Mat(), kpts1, desc1)
            akaze.detectAndCompute(screenMat, Mat(), kpts2, desc2)
            val matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING)
            val knnMatches = ArrayList<MatOfDMatch>()
            matcher.knnMatch(desc1, desc2, knnMatches, 2)
            val ratioThreshold = 0.8f // Nearest neighbor matching ratio
            val listOfMatched1 = ArrayList<KeyPoint>()
            val listOfMatched2 = ArrayList<KeyPoint>()
            val listOfKeypoints1 = kpts1.toList()
            val listOfKeypoints2 = kpts2.toList()
            for (i in 0 until knnMatches.size) {
                val matches = knnMatches[i].toArray()
                val dist1 = matches[0].distance
                val dist2 = matches[1].distance
                if (dist1 < ratioThreshold * dist2) {
                    listOfMatched1.add(listOfKeypoints1[matches[0].queryIdx])
                    listOfMatched2.add(listOfKeypoints2[matches[0].trainIdx])
                }
            }
            return Pair(-1, -1)
        }
    }
}