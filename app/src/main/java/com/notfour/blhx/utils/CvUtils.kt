package com.notfour.blhx.utils

import android.util.Log
import com.notfour.blhx.jni.ScreenShotUtils
import org.opencv.calib3d.Calib3d
import org.opencv.core.*
import org.opencv.features2d.DescriptorExtractor
import org.opencv.features2d.DescriptorMatcher
import org.opencv.features2d.FeatureDetector
import org.opencv.features2d.Features2d
import org.opencv.imgcodecs.Imgcodecs
import java.util.*


/**
 * Created with author.
 * Description:
 * Date: 2019/1/13
 * Time: 21:06
 */
class CvUtils {
    companion object {
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
            picpath: String,
            degree: Int,
            x1: Int,
            y1: Int,
            x2: Int,
            y2: Int,
            alpha: Int
        ): IntArray {
            val templateKeyPoints = MatOfKeyPoint()
            //指定特征点算法SURF
            val featureDetector = FeatureDetector.create(FeatureDetector.SURF)
            val templateImage = Imgcodecs.imread(picpath)
            //获取模板图的特征点
            featureDetector.detect(templateImage, templateKeyPoints)
            //提取模板图的特征点
            val templateDescriptors = MatOfKeyPoint()
            val descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SURF)
            Log.e(TAG, "提取模板图的特征点")
            descriptorExtractor.compute(templateImage, templateKeyPoints, templateDescriptors)

            //显示模板图的特征点图片
            val outputImage = Mat(templateImage.rows(), templateImage.cols(), Imgcodecs.CV_LOAD_IMAGE_COLOR)
            Log.e(TAG, "在图片上显示提取的特征点")
            Features2d.drawKeypoints(
                templateImage,
                templateKeyPoints,
                outputImage,
                Scalar(255.toDouble(), 0.toDouble(), 0.toDouble()),
                0
            )

            //获取原图的特征点
            val originalKeyPoints = MatOfKeyPoint()
            val originalDescriptors = MatOfKeyPoint()
            val screenshot = "${ScreenShotUtils.SCREENSHOT_DIR}/screen.png"
            ScreenShotUtils.takeScreenshot(screenshot)
            val originalImage = Imgcodecs.imread(screenshot)
            featureDetector.detect(originalImage, originalKeyPoints)
            Log.e(TAG, "提取原图的特征点")
            descriptorExtractor.compute(originalImage, originalKeyPoints, originalDescriptors)

            val matches = LinkedList<MatOfDMatch>()
            val descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED)
            Log.e(TAG, "寻找最佳匹配")
            /**
             * knnMatch方法的作用就是在给定特征描述集合中寻找最佳匹配
             * 使用KNN-matching算法，令K=2，则每个match得到两个最接近的descriptor，然后计算最接近距离和次接近距离之间的比值，当比值大于既定值时，才作为最终match。
             */
            descriptorMatcher.knnMatch(templateDescriptors, originalDescriptors, matches, 2)

            Log.e(TAG, "计算匹配结果")
            val goodMatchesList = LinkedList<DMatch>()

            //对匹配结果进行筛选，依据distance进行筛选
            val nndrRatio = 0.7f
            matches.forEach { match ->
                val dmatcharray = match.toArray()
                val m1 = dmatcharray[0]
                val m2 = dmatcharray[1]
                if (m1.distance <= m2.distance * nndrRatio) {
                    goodMatchesList.addLast(m1)
                }
            }

            var matchesPointCount = goodMatchesList.size
            //当匹配后的特征点大于等于 4 个，则认为模板图在原图中，该值可以自行调整
            if (matchesPointCount >= 4) {
                Log.e(TAG, "模板图在原图匹配成功！")

                val templateKeyPointList = templateKeyPoints.toList()
                val originalKeyPointList = originalKeyPoints.toList()
                val objectPoints = LinkedList<Point>()
                val scenePoints = LinkedList<Point>()
                goodMatchesList.forEach { goodMatch ->
                    objectPoints.addLast(templateKeyPointList.get(goodMatch.queryIdx).pt)
                    scenePoints.addLast(originalKeyPointList.get(goodMatch.trainIdx).pt)
                }
                val objMatOfPoint2f = MatOfPoint2f()
                objMatOfPoint2f.fromList(objectPoints)
                val scnMatOfPoint2f = MatOfPoint2f()
                scnMatOfPoint2f.fromList(scenePoints)
                //使用 findHomography 寻找匹配上的关键点的变换
                val homography = Calib3d.findHomography(objMatOfPoint2f, scnMatOfPoint2f, Calib3d.RANSAC, 3.toDouble())

                /**
                 * 透视变换(Perspective Transformation)是将图片投影到一个新的视平面(Viewing Plane)，也称作投影映射(Projective Mapping)。
                 */
                val templateCorners = Mat(4, 1, CvType.CV_32FC2)
                val templateTransformResult = Mat(4, 1, CvType.CV_32FC2)
                templateCorners.put(0, 0, intArrayOf(0, 0))
                templateCorners.put(1, 0, intArrayOf(templateImage.cols(), 0))
                templateCorners.put(2, 0, intArrayOf(templateImage.cols(), templateImage.rows()))
                templateCorners.put(3, 0, intArrayOf(0, templateImage.rows()))
                //使用 perspectiveTransform 将模板图进行透视变以矫正图象得到标准图片
                Core.perspectiveTransform(templateCorners, templateTransformResult, homography);

                //矩形四个顶点
                val pointA = templateTransformResult.get(0, 0)
                val pointB = templateTransformResult.get(1, 0)
                val pointC = templateTransformResult.get(2, 0)
                val pointD = templateTransformResult.get(3, 0)

                //指定取得数组子集的范围
                val rowStart = pointA[1]
                val rowEnd = pointC[1]
                val colStart = pointD[0]
                val colEnd = pointB[0]
                val subMat = originalImage.submat(rowStart.toInt(), rowEnd.toInt(), colStart.toInt(), colEnd.toInt())
                Imgcodecs.imwrite("/Users/niwei/Desktop/opencv/原图中的匹配图.jpg", subMat);

                //将匹配的图像用用四条线框出来
//                Core.line(originalImage, new Point (pointA), new Point (pointB), new Scalar (0, 255, 0), 4);//上 A->B
//                Core.line(originalImage, new Point (pointB), new Point (pointC), new Scalar (0, 255, 0), 4);//右 B->C
//                Core.line(originalImage, new Point (pointC), new Point (pointD), new Scalar (0, 255, 0), 4);//下 C->D
//                Core.line(originalImage, new Point (pointD), new Point (pointA), new Scalar (0, 255, 0), 4);//左 D->A

                val goodMatches = MatOfDMatch()
                goodMatches.fromList(goodMatchesList)
                val matchOutput = Mat(
                    originalImage.rows() * 2,
                    originalImage.cols() * 2,
                    Imgcodecs.CV_LOAD_IMAGE_COLOR
                )
                Features2d.drawMatches(
                    templateImage,
                    templateKeyPoints,
                    originalImage,
                    originalKeyPoints,
                    goodMatches,
                    matchOutput,
                    Scalar(
                        0.0,
                        255.0,
                        0.0
                    ), Scalar(
                        255.0, 0.0, 0.0
                    ), MatOfByte(), 2
                )

                Imgcodecs.imwrite("/Users/niwei/Desktop/opencv/特征点匹配过程.jpg", matchOutput)
                Imgcodecs.imwrite("/Users/niwei/Desktop/opencv/模板图在原图中的位置.jpg", originalImage)
            } else {
                Log.e(TAG, "模板图不在原图中！")
            }

            Imgcodecs.imwrite("/Users/niwei/Desktop/opencv/模板特征点.jpg", outputImage)
            return IntArray(0)
        }
    }
}