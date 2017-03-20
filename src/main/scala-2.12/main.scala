import java.io.File

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import edu.wpi.rail.jrosbridge._
import edu.wpi.rail.jrosbridge.callback.TopicCallback
import edu.wpi.rail.jrosbridge.messages.Message
import org.bytedeco.javacpp.opencv_calib3d.{CV_RANSAC, findHomography}
import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_features2d._
import org.bytedeco.javacpp.opencv_imgcodecs._
import org.bytedeco.javacpp.opencv_imgproc._
import org.dhash.util
import org.pmw.tinylog.Logger
import ros.msgs.sensor_msgs.Image
import ros.tools.MessageUnpacker

import scala.collection.immutable.Map

class ROSPubSub() {
  val conn = new Ros() //implied localhost 9090
  //and rosbridge_server running
  conn.connect()

  /*
  def draw_box
  def train
    def build_sign_SIFTS
    def matching
    def eval
 */
}

class sign_localizer(imdir: String) {
  private val cv = new util.opencv
  private val mapper = new ObjectMapper()
  private val matcher = new FlannBasedMatcher()
  private val signs = train(imdir)

  def train(imdir: String): Map[String, (KeyPointVector, Mat)] = {
    """Generates keypoints w/ SIFT on all images
      |In imdir. 1 class per filename.
    """.stripMargin
    val fdir = new File(getClass.getResource(imdir).toURI)
    val kps = for (f <- fdir.listFiles)
      yield cv.SIFT_features(imread(f.toString, COLOR_BGR2GRAY))
    val names = for (f <- fdir.listFiles) yield f.getName

    Map() ++ (names zip kps)
  }

  def process_image(gs_im: Mat): (Map[String, Float], Mat) = {
    val t1 = System.currentTimeMillis()
    //Make sure the image is in grayscale
    val (im_feats, im_desc) = cv.SIFT_features(gs_im)

    val costs = for ((_, (_, desc)) <- signs.toSeq)
      yield {
        //Match against all images in the training set
        val matches = new DMatchVector()
        matcher.`match`(im_desc, desc, matches)
        //TODO: Off by one?
        val dist = for (d_idx <- 1 to matches.size().toInt) yield {
          matches
            .get(d_idx)
            .distance()
        }
        dist
      }
    val normed_cost = for (c <- costs)
    //find the summed distance of all keypoints
    //and scale all training images "likelihood" based on that
      yield {
        val minc = costs.map(_.sum).reduce(_.min(_))
        val maxc = costs.map(_.sum).reduce(_.max(_))
        1 - (c.sum - minc) / (maxc - minc)
      }
    //find the homography of the most likely class with RANSAC
    val most_likely_class = (signs.keys zip normed_cost)
      .reduce((x, y) => if (x._2 > y._2) x else y)
      ._1

    val best_image_match = signs.get(most_likely_class) match {
      case Some(x) => x
      case None => throw new NullPointerException("What the heck ???") //lol
    }

    val best_matches = new DMatchVector()
    matcher.`match`(im_desc, best_image_match._2, best_matches)

    val (p1, p2) =
      cv.KeyPointsToP2V(best_matches, im_feats, best_image_match._1)
    val inliers = new Mat()
    //the magic number 3 here is the RANSAC reprojection threshold
    //http://docs.opencv.org/2.4/modules/calib3d/doc/camera_calibration_and_3d_reconstruction.html?highlight=findhomography#findhomography
    val homography =
    findHomography(cv.toMat(p1), cv.toMat(p2), inliers, CV_RANSAC, 3)

    val result = new Mat()
    val most_likely_im =
      cv.imopen("/sign_images/sign_images/" + most_likely_class)
    warpPerspective(most_likely_im,
      result,
      homography,
      new Size(2 * math.max(most_likely_im.cols, gs_im.cols),
        math.max(most_likely_im.rows, gs_im.rows())))

    val t2 = System.currentTimeMillis()
    println(signs.keys zip normed_cost)
    println(t2 - t1)
    (Map() ++ (signs.keys zip normed_cost), result)
  }

  object process_image_callback extends TopicCallback {
    override def handleMessage(msg: Message): Unit = {
      println("New message")
      msg.setMessageType("sensor_msgs/Image")
      val unpacker = new MessageUnpacker[Image](classOf[Image])
      val im_msg: Image = unpacker.unpackRosMessage(
        mapper.readValue[JsonNode](msg.toString, classOf[JsonNode]))
      //Matrix of data, unsigned
      var im = new Mat(im_msg.data, false)
      //A 3-channel, 2D image
      im = im.reshape(3, 2, Array(im_msg.height, im_msg.width))
      val gs_im = new Mat()
      //im_msg.encoding is rgb8
      cvtColor(im, gs_im, COLOR_RGB2GRAY)
      val probs = process_image(gs_im)
      Logger.trace(s"message probs: ${probs}")
    }
  }

}

object main extends App {
  override def main(args: Array[String]): Unit = {
    //run_tests()
    /*
    val cv = new util.opencv
    val vision = new sign_localizer("/sign_images/sign_images")
    for (i <- 0 to 10)
      yield
        vision.process_image(
          cv.imopen("/sign_images/scenes/leftturn_scene.jpg"))
    */
    val ros = new Ros()
    ros.connect()
    //make SIFT features all sign images
    //val name_feats = cv.train("/sign_images/sign_images")
    //register a publisher that publishes an Image
    val vision = new sign_localizer("/sign_images/sign_images")
    val bbox_pub = new Topic(ros, "/predicted_sign", "sensor_msgs/Image")
    //register a subscriber that processes Image and publishes
    val image_sub = new Topic(ros, "/camera/image_raw", "sensor_msgs/Image")
    image_sub.subscribe(vision.process_image_callback)

    //matches Image name_feats with FLANN for O(n) lookup
    //outputs average cost. Confidence threshold argument
    println("connected and running")
    sys.addShutdownHook(
      ros.disconnect()
    )
    while (ros.isConnected) {
      Thread.sleep(100)
    }


  }

  def run_tests(): Unit = {
    val tests: util.tests = new util.tests
    Logger.trace("Starting basic test...")
    tests.imshow_test()
    Logger.trace("Basic test complete")
    Logger.trace("Starting SIFT test")
    tests.SIFT_test()
    Logger.trace("SIFT test complete")

  }
}
