name := """thinkit-reactive-sample"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "com.typesafe.slick" %% "slick" % "3.1.1",
  "com.typesafe.play" %% "play-slick" % "2.0.0",
  "com.h2database" % "h2" % "1.3.176",
  "com.zaxxer" % "HikariCP" % "2.3.7"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
