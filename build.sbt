name := "cv-test-drive"

version := "1.0"

scalaVersion := "2.12.1"

scalacOptions := Seq("-unchecked", "-deprecation")

fork in run := true

javaCppPresetLibs ++= Seq("opencv" -> "3.2.0",
                          "ffmpeg" -> "3.2.1",
                          "cuda" -> "8.0")

libraryDependencies ++= Seq(
  "org.tinylog" % "tinylog" % "1.2",
  "edu.wpi.rail" % "jrosbridge" % "0.2.0",
  "org.bytedeco" % "javacv-platform" % "1.3.2",
  "edu.brown.cs.burlap" % "java_rosbridge" % "2.0.1"
)
