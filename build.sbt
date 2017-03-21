import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

name := "akka-websockets-examples"

organization := "org.ardlema"

version := "0.1.0"

scalaVersion := "2.11.7"

resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
)

libraryDependencies ++= Seq(

  //"com.chuusai" %% "shapeless" % "2.3.2",
  //"org.typelevel" %% "cats" % "0.8.0",

  //"com.typesafe.akka" %% "akka-slf4j" % "2.4.12",
    //"com.typesafe.akka" %% "akka-stream-testkit" % "2.4.12",
    //"com.typesafe.akka" %% "akka-testkit" % "2.4.12" % "test",

    //"com.typesafe.akka" %% "akka-http-experimental" % "2.4.11",
    //"com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.11",
    //"com.typesafe.akka" %% "akka-http-testkit" % "2.4.11",

  "com.typesafe.akka" %% "akka-http-core" % "10.0.5",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.11.1",
  "com.typesafe.akka" %% "akka-actor" % "2.4.17",
  "com.typesafe.akka" %% "akka-stream" % "2.4.17",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
)

initialCommands := "import org.ardlema.akkawebsocketsexamples._"

SbtScalariform.scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(RewriteArrowSymbols, true)