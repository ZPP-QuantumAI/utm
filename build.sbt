ThisBuild / scalaVersion := "2.13.12"
ThisBuild / organization := "pl.mimuw.zpp.quantumai"

lazy val root = (project in file("."))
  .settings(
    name := "utm",
    libraryDependencies ++= Seq(
      "dev.zio"           %% "zio"                % "2.0.21",
      "dev.zio"           %% "zio-kafka"          % "2.7.2",
      "dev.zio"           %% "zio-json"           % "0.6.2",
      "org.mongodb.scala" %% "mongo-scala-driver" % "4.11.1"
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
