import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_imgcodecs._
import org.bytedeco.javacpp.opencv_imgproc._


import javax.swing.JFrame
import org.bytedeco.javacv.OpenCVFrameConverter
import org.bytedeco.javacv.CanvasFrame

import org.pmw.tinylog.Logger

import java.io.File

object main extends App {
  override def main(args: Array[String]): Unit = {
    Logger.trace("Starting basic test...")
    test()
    Logger.trace("Test complete")
  }
  def show(mat: Mat, title: String = "Showing image"): Unit = {
    val converter = new OpenCVFrameConverter.ToMat()
    val canvas = new CanvasFrame(title, 1)

    canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    canvas.showImage(converter.convert(mat))
  }
  def test(impath: String = "/lena.bmp") : Unit = {
    val imFile = new File(getClass.getResource(impath).toURI)
    show(imread(imFile.toString))
  }
}

