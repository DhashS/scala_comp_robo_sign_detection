import java.awt.event.WindowEvent

import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_imgcodecs._
import org.bytedeco.javacpp.opencv_imgproc._
import org.bytedeco.javacpp.BytePointer
import org.bytedeco.javacpp.opencv_xfeatures2d.SIFT
import org.bytedeco.javacpp.opencv_features2d._
import org.pmw.tinylog.Logger
import java.io.File
import javax.swing.{ImageIcon, JLabel, JPanel}

import scala.concurrent._
import ExecutionContext.Implicits.global
import org.dhash.util
import edu.wpi.rail.jrosbridge._
import edu.wpi.rail.jrosbridge.callback.TopicCallback
import edu.wpi.rail.jrosbridge.messages.Message
import ros.tools.MessageUnpacker
import ros.msgs.sensor_msgs.Image
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}


object main extends App {
  override def main(args: Array[String]): Unit = {
    //run_tests()
    val ros = new Ros()
    ros.connect()
    val cv = new sign_localizer
    //make SIFT features all sign images
    val name_feats = cv.train("/sign_images/sign_images")
    //register a publisher that publishes an Image
    val bbox_pub = new Topic(ros, "/predicted_sign", "sensor_msgs/Image")
    //register a subscriber that processes Image and publishes
    val image_sub = new Topic(ros, "/camera/image_raw", "sensor_msgs/Image")
    image_sub.subscribe(cv.process_image)
    //matches Image name_feats with FLANN for O(n) lookup
    //outputs average cost. Confidence threshold argument

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

class sign_localizer() {
  implicit val cv = new util.opencv
  implicit val mapper = new ObjectMapper()
  implicit val cvs = new util.canvas("Image")

  def train(imdir: String): Array[(String, KeyPointVector)] = {
    """Generates keypoints w/ SIFT on all images
      |In imdir. 1 class per filename.
    """.stripMargin
    val fdir = new File(getClass.getResource(imdir).toURI)
    val kps = for (f <- fdir.listFiles) yield
      cv.SIFT_features(imread(f.toString, COLOR_BGR2GRAY))
    val names = for (f <- fdir.listFiles) yield
      f.getName
    names zip kps
  }
  object process_image extends TopicCallback {
    override def handleMessage(msg: Message): Unit = {
      msg.setMessageType("sensor_msgs/Image")
      val unpacker = new MessageUnpacker[Image](classOf[Image])
      val im_msg: Image = unpacker.unpackRosMessage(mapper.readValue[JsonNode](msg.toString, classOf[JsonNode]))
      var im = new Mat(im_msg.data, false)
        im = im.reshape(im_msg.height, im_msg.width)
      cvs.imshow(im)
      Logger.trace(im)

    }

  }
}
