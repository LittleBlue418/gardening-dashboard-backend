name := """gardening-dashboard-backend"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "3.3.4"

// Dev server port for `sbt run`. Not Play's default 9000, which clashes with work services.
// The frontend dev server (port 7711) proxies /api here.
PlayKeys.playDefaultPort := 7710

libraryDependencies ++= Seq(
  guice,
  "org.playframework" %% "play-json" % "3.0.4",
  "org.typelevel" %% "cats-effect" % "3.7.0",
  "com.iheart" %% "ficus" % "1.5.2",
  "net.codingwell" %% "scala-guice" % "6.0.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test
)

scalacOptions ++= Seq(
  "-deprecation", // Warn about deprecated features
  "-feature", // Warn about features that should be imported explicitly
  "-unchecked", // Warn about unchecked type patterns
  "-Xlint", // Enable all linting warnings
  "-Wunused:imports", // Warn about unused imports
  "-Wunused:locals", // Warn about unused local variables
  "-Wvalue-discard" // Warn when non-Unit values are discarded
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
