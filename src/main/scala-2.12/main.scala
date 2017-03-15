import java.awt.event.WindowEvent

import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_imgcodecs._
import org.bytedeco.javacpp.opencv_imgproc._
import org.bytedeco.javacpp.opencv_xfeatures2d.SIFT
import org.bytedeco.javacpp.opencv_features2d._

import org.pmw.tinylog.Logger
import java.io.File

import scala.concurrent._
import ExecutionContext.Implicits.global

import org.dhash.util


object main extends App {
  override def main(args: Array[String]): Unit = {
    run_tests()
  }
  def run_tests(): Unit = {
    val tests: util.tests = new util.tests
    Logger.trace("Starting basic test...")
    tests.imshow_test()
    Logger.trace("Basic test complete")
    Logger.trace("Starting SIFT test")
    tests.SIFT_test()
    Logger.trace("SIFT test complete")

    //hack, closes all open canvas
    //TBD
    val cvs = new util.canvas("Closing window")
    cvs.close()
  }
}

class comprobo {
  def process_frame(): Unit = {

  }
  /*
  def draw_box
  def train
    def build_sign_SIFTS
    def matching
    def eval
  */
}
