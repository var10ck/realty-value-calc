ThisBuild / scalaVersion     := "2.13.10"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
    .settings(
      name := "area-price-computation",
      libraryDependencies ++= Seq(
        "dev.zio" %% "zio"      % "2.0.2",
        "dev.zio" %% "zio-test" % "2.0.2" % Test,
        "io.d11" %% "zhttp"     % "2.0.0-RC10",
        "dev.zio" %% "zio-json" % "0.3.0-RC11"
      ),
      libraryDependencies ++= Seq(
        "io.circe" %% "circe-core",
        "io.circe" %% "circe-generic",
        "io.circe" %% "circe-parser"
      ).map(_ % "0.14.1"),
      testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
    )
