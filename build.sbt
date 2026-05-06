name := """gardening-dashboard-backend"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.18"

libraryDependencies ++= Seq(
  guice,
  ws, // WebService client for making HTTP requests (OAuth needs this)
  "org.mongodb.scala" %% "mongo-scala-driver" % "5,6,4",
  "org.playframework" %% "play-json" % "3.0.4",
  "com.iheart" %% "ficus" % "1.5.2",
  "net.codingwell" %% "scala-guice" % "6.0.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test
)

scalacOptions ++= Seq(
  "-deprecation",           // Warn about deprecated features
  "-feature",               // Warn about features that should be imported explicitly
  "-unchecked",             // Warn about unchecked type patterns
  "-Xlint",                 // Enable all linting warnings
  "-Wunused:imports",       // Warn about unused imports
  "-Wunused:locals",        // Warn about unused local variables
  "-Wvalue-discard"         // Warn when non-Unit values are discarded
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
