ThisBuild / scalaVersion     := "2.13.12"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "pl.mimuw.zpp.quantumai"

lazy val root = (project in file("."))
  .settings(
    name := "utm",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"       % "2.0.21",
      "dev.zio" %% "zio-kafka" % "2.3.0",
      "dev.zio" %% "zio-json"  % "0.5.0",
      "dev.zio" %% "zio-test"  % "2.0.21" % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
