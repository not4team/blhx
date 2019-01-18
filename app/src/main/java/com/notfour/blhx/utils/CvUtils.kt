package com.notfour.blhx.utils

import android.os.SystemClock
import android.util.Log
import com.notfour.blhx.jni.ScreenShotUtils
import org.opencv.calib3d.Calib3d
import org.opencv.core.*
import org.opencv.core.Scalar
import org.opencv.features2d.AKAZE
import org.opencv.features2d.DescriptorMatcher
import org.opencv.features2d.Features2d
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.util.*


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
         * 特征点匹配
         * @param picpath-将要寻找的图片文件名
         * @param degree-寻找精度，范围：1 ~ 100，当是100时为完全匹配
         * @param x1,y1-欲寻找的区域左上角顶点屏幕坐标
         * @param x2,y2-欲寻找的区域右下角顶点屏幕坐标
         * @param alpha-忽略的颜色值（透明色） 若无请填 0
         * @return 找到的图片的左上角顶点坐标，如未找到则返回 -1，-1
         */
        fun findImageMatchFeature(
            picPath: String,
            degree: Int,
            x1: Int,
            y1: Int,
            x2: Int,
            y2: Int,
            alpha: Int
        ): Pair<Int, Int> {
            val startTime = SystemClock.currentThreadTimeMillis()
            Log.e(TAG, "findImageMatchFeature start time ${startTime}")
            val picMat = Imgcodecs.imread(picPath)
            val screenPath = "/sdcard/cvtest/screen.png"
//            ScreenShotUtils.takeScreenshot(screenPath)
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
            val goodMatchesList = LinkedList<DMatch>()
            for (i in 0 until knnMatches.size) {
                val matches = knnMatches[i].toArray()
                val dist1 = matches[0].distance
                val dist2 = matches[1].distance
                if (dist1 < ratioThreshold * dist2) {
                    listOfMatched1.add(listOfKeypoints1[matches[0].queryIdx])
                    listOfMatched2.add(listOfKeypoints2[matches[0].trainIdx])
                    goodMatchesList.addLast(matches[0])
                }
            }
            Log.e(TAG, "# Keypoints 1:${listOfKeypoints1.size}")
            Log.e(TAG, "# Keypoints 2:${listOfKeypoints2.size}")
            Log.e(TAG, "# Matches:${listOfMatched1.size}")
            val percent = listOfMatched1.size.toFloat() / listOfKeypoints1.size
            Log.e(TAG, "# Percent:${percent}")

            val objectPoints = LinkedList<Point>()
            val scenePoints = LinkedList<Point>()
            goodMatchesList.forEach { match ->
                objectPoints.addLast(listOfKeypoints1.get(match.queryIdx).pt)
                scenePoints.addLast(listOfKeypoints2.get(match.trainIdx).pt)
            }
            val goodMatches = MatOfDMatch()
            goodMatches.fromList(goodMatchesList)
            //-- Draw matches
            val imgMatches = Mat()
            Features2d.drawMatches(
                picMat, kpts1, screenMat, kpts2, goodMatches, imgMatches, Scalar.all(-1.0),
                Scalar.all(-1.0), MatOfByte(), Features2d.NOT_DRAW_SINGLE_POINTS
            )
            val objMatOfPoint2f = MatOfPoint2f()
            objMatOfPoint2f.fromList(objectPoints)
            val scnMatOfPoint2f = MatOfPoint2f()
            scnMatOfPoint2f.fromList(scenePoints)
            val homography = Calib3d.findHomography(objMatOfPoint2f, scnMatOfPoint2f, Calib3d.RANSAC, 3.0)
            val picCorners = Mat(4, 1, CvType.CV_32FC2)
            val sceneCorners = Mat()
            val objCornersData = FloatArray(8)
            picCorners.get(0, 0, objCornersData)
            objCornersData[0] = 0f
            objCornersData[1] = 0f
            objCornersData[2] = picMat.cols().toFloat()
            objCornersData[3] = 0f
            objCornersData[4] = picMat.cols().toFloat()
            objCornersData[5] = picMat.rows().toFloat()
            objCornersData[6] = 0f
            objCornersData[7] = picMat.rows().toFloat()
            picCorners.put(0, 0, objCornersData)

            Core.perspectiveTransform(picCorners, sceneCorners, homography)
            val sceneCornersData = FloatArray(8)
            sceneCorners.get(0, 0, sceneCornersData)
            val pointA = Point((sceneCornersData[0] + picMat.cols().toDouble()), sceneCornersData[1].toDouble())
            val pointB = Point((sceneCornersData[2] + picMat.cols()).toDouble(), sceneCornersData[3].toDouble())
            val pointC = Point((sceneCornersData[4] + picMat.cols()).toDouble(), sceneCornersData[5].toDouble())
            val pointD = Point((sceneCornersData[6] + picMat.cols()).toDouble(), sceneCornersData[7].toDouble())
            Log.e(TAG, "left top corner point x:${pointA.x} y:${pointA.y}")
            Imgproc.line(imgMatches, pointA, pointB, Scalar(0.0, 255.0, 0.0), 4)
            Imgproc.line(imgMatches, pointB, pointC, Scalar(0.0, 255.0, 0.0), 4)
            Imgproc.line(imgMatches, pointC, pointD, Scalar(0.0, 255.0, 0.0), 4)
            Imgproc.line(imgMatches, pointD, pointA, Scalar(0.0, 255.0, 0.0), 4)
            Imgcodecs.imwrite("${ScreenShotUtils.SCREENSHOT_DIR}/screen_cv.png", imgMatches)

            val endTime = SystemClock.currentThreadTimeMillis()
            Log.e(TAG, "findImageMatchFeature end time ${endTime}")
            Log.e(TAG, "findImageMatchFeature total time ${(endTime - startTime) / 1000}s")
            if (percent * 100 < degree) {
                return Pair(-1, -1)
            }
            return Pair(pointA.x.toInt(), pointA.y.toInt())
        }

        /**
         * 模糊区域找图
         * 模板匹配
         * @param picpath-将要寻找的图片文件名
         * @param degree-寻找精度，范围：1 ~ 100，当是100时为完全匹配
         * @param x1,y1-欲寻找的区域左上角顶点屏幕坐标
         * @param x2,y2-欲寻找的区域右下角顶点屏幕坐标
         * @param alpha-忽略的颜色值（透明色） 若无请填 0
         * @return 找到的图片的左上角顶点坐标，如未找到则返回 -1，-1
         */
        fun findImageMatchTemplate(
            picPath: String,
            degree: Int,
            x1: Int,
            y1: Int,
            x2: Int,
            y2: Int,
            alpha: Int
        ): Pair<Int, Int> {
            val startTime = SystemClock.currentThreadTimeMillis()
            Log.e(TAG, "findImageMatchTemplate start time ${startTime}")
            var match_method = Imgproc.TM_SQDIFF
            val screenPath = "/sdcard/cvtest/screen.png"
            val img = Imgcodecs.imread(screenPath)
            val templ = Imgcodecs.imread(picPath)
            val result = Mat()
            val img_display = Mat()
            img.copyTo(img_display)
            val result_cols = img.cols() - templ.cols() + 1
            val result_rows = img.rows() - templ.rows() + 1
            result.create(result_rows, result_cols, CvType.CV_32FC1)
            Imgproc.matchTemplate(img, templ, result, match_method)
            Core.normalize(result, result, 0.0, 1.0, Core.NORM_MINMAX, -1, Mat())
            var matchLoc: Point? = null
            val mmr = Core.minMaxLoc(result)
            if (match_method === Imgproc.TM_SQDIFF || match_method === Imgproc.TM_SQDIFF_NORMED) {
                matchLoc = mmr.minLoc
            } else {
                matchLoc = mmr.maxLoc
            }
            Imgproc.rectangle(
                img_display, matchLoc, Point(matchLoc.x + templ.cols(), matchLoc.y + templ.rows()),
                Scalar(0.0, 0.0, 0.0), 2, 8, 0
            )
            Imgproc.rectangle(
                result, matchLoc, Point(matchLoc.x + templ.cols(), matchLoc.y + templ.rows()),
                Scalar(0.0, 0.0, 0.0), 2, 8, 0
            )
            Imgcodecs.imwrite("${ScreenShotUtils.SCREENSHOT_DIR}/img_display_cv.png", img_display)
            Imgcodecs.imwrite("${ScreenShotUtils.SCREENSHOT_DIR}/result_cv.png", result)
            val endTime = SystemClock.currentThreadTimeMillis()
            Log.e(TAG, "findImageMatchTemplate end time ${endTime}")
            Log.e(TAG, "findImageMatchTemplate total time ${(endTime - startTime) / 1000}s")
            return Pair(matchLoc.x.toInt(), matchLoc.y.toInt())
        }
    }
}