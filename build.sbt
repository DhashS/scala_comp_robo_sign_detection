name := "cv-test-drive"

version := "1.0"

scalaVersion := "2.12.1"

scalacOptions := Seq("-unchecked", "-deprecation")

fork in run := true

libraryDependencies ++= Seq(
  "org.tinylog" % "tinylog" % "1.2",
  "org.bytedeco" % "javacv-platform" % "1.3.1")