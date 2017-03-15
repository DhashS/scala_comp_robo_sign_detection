package org.dhash.util

import java.awt.event.WindowEvent
import java.io.File
import javax.swing.JFrame

import org.bytedeco.javacpp.opencv_core.{KeyPointVector, Mat, Scalar}
import org.bytedeco.javacpp.opencv_features2d.{DrawMatchesFlags, drawKeypoints}
import org.bytedeco.javacpp.opencv_imgcodecs.imread
import org.bytedeco.javacpp.opencv_imgproc.{COLOR_BGR2GRAY, cvtColor}
import org.bytedeco.javacpp.opencv_xfeatures2d.SIFT
import org.bytedeco.javacv.{CanvasFrame, OpenCVFrameConverter}


import org.pmw.tinylog.Logger


class opencv {
  def imopen(respath: String) : Mat = {
    val imFile = new File(getClass.getResource(respath).toURI)
    val imName = imFile.toString
    imread(imName)
  }
  def SIFT_features(im: Mat) : KeyPointVector = {
    val pts = new KeyPointVector()
    SIFT.create().detect(im, pts)
    pts
  }
}
class canvas(title: String) {
  var canvas = new CanvasFrame(title)
  canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

  def imshow(mat: Mat = new Mat): CanvasFrame = {
    val converter = new OpenCVFrameConverter.ToMat()
    canvas.showImage(converter.convert(mat))
    canvas
  }
  def close(): Unit = {
    canvas.dispatchEvent(new WindowEvent(canvas, WindowEvent.WINDOW_CLOSING))
  }
}
class tests {
  def imshow_test(impath: String = "/lena.bmp") : Unit = {
    val opencv = new opencv
    val cvs = new canvas("Test image")
    val im = opencv.imopen(impath)
    cvs.imshow(im)
    Logger.trace(s"Showing $impath in a new JFrame")
    Logger.trace(s"Test complete, closing JFrame")
  }

  def SIFT_test(): Unit = {
    //show a gray scale image
    val cv = new opencv
    val gs_im = new Mat
    cvtColor(cv.imopen("/lena.bmp"), gs_im, COLOR_BGR2GRAY)
    val cvs = new canvas("Grayscale Lena")
    cvs.imshow(gs_im)

    //get SIFT keypoints and draw them on the initial image
    var SIFT_keypoints = new KeyPointVector()
    SIFT.create().detect(gs_im, SIFT_keypoints)
    val img_with_feats = new Mat()
    drawKeypoints(gs_im, SIFT_keypoints, img_with_feats, Scalar.all(-1), DrawMatchesFlags.DRAW_RICH_KEYPOINTS)

    //open another window with the drawn keypoints
    val cvs2 = new canvas("Image with SIFT keypoints")
    cvs2.imshow(img_with_feats)
  }
}