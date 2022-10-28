ThisBuild / scalaVersion     := "2.13.9"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "org.scalalazy"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
    .settings(
      name := "innoagency-hackathon",
      libraryDependencies ++= Dependencies.zio ++
          Dependencies.zioConfig ++
          Dependencies.liquibase ++
          Dependencies.testContainers ++
          Dependencies.quill ++
//          Dependencies.tapir ++
          Seq(
            Dependencies.zioHttp,
            Dependencies.scalaTest,
            Dependencies.catsCore,
            Dependencies.logback,
            Dependencies.zioJson,
            Dependencies.bcrypt,
            Dependencies.zioPrelude
          ),
      testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
      addCompilerPlugin(Dependencies.kindProjector)
    )
