import com.tuplejump.sbt.yeoman.Yeoman

import scalariform.formatter.preferences._

name := "partner"

version := "3.0.1"

scalaVersion := "2.11.7"

resolvers := ("Atlassian Releases" at "https://maven.atlassian.com/public/") +: resolvers.value

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Local Play Repository" at "file://Users/alex/.ivy2/local"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette" % "3.0.2",
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "net.codingwell" %% "scala-guice" % "4.0.0",
  "net.ceedubs" %% "ficus" % "1.1.2",
  "com.adrianhurt" %% "play-bootstrap3" % "0.4.4-P24",
  "org.webjars" % "bootstrap" % "3.1.1",
  "com.mohiva" %% "play-silhouette-testkit" % "3.0.2" % "test",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
  "org.webjars.bower" % "font-awsome" % "4.4.0",
  specs2 % Test,
  "com.typesafe.play" %% "play-slick" % "1.0.1",
  "com.typesafe.play" %% "play-slick-evolutions" % "1.0.1",
  "org.postgresql" % "postgresql" % "9.4-1202-jdbc42",
  "com.cloudinary" % "cloudinary-http44" % "1.2.2",
  cache,
  evolutions,
  filters
)

lazy val common = ProjectRef(file("../common"),"common")

lazy val root = (project in file(".")).enablePlugins(PlayScala, JavaAppPackaging).aggregate(common).dependsOn(common)

includeFilter in (Assets, LessKeys.less) := "*.less"

routesGenerator := InjectedRoutesGenerator

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xlint", // Enable recommended additional warnings.
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
  "-Ywarn-numeric-widen" // Warn when numerics are widened.
)
//********************************************************
// Yeoman settings
//********************************************************
Yeoman.yeomanSettings


//********************************************************
// Scalariform settings
//********************************************************

defaultScalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(FormatXml, false)
  .setPreference(DoubleIndentClassDeclaration, false)
  .setPreference(PreserveDanglingCloseParenthesis, true)

//********************************************************
// heroku settings
// https://github.com/heroku/sbt-heroku#configuring-the-plugin
//********************************************************
herokuAppName in Compile := Map(
  "test" -> "ancient-shore-6366",
  "stg"  -> "your-heroku-app-stage",
  "prod" -> "your-heroku-app-prod"
  ).getOrElse(sys.props("appEnv"), "ancient-shore-6366")

herokuJdkVersion in Compile := "1.8"
herokuConfigVars in Compile := Map(
  "NPM_CONFIG_PRODUCTION" -> "false",
  "PLAY_PROD_CONF_FILE" -> "application.prod.conf",
  "JAVA_OPTS" -> "-XX:+UseCompressedOops"
)

fork in run := true