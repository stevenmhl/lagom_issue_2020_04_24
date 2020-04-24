organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.13.2"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % Test

val baseImplDepencencies = Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "com.iheart" %% "ficus" % "1.4.7"
)


lazy val `app` = (project in file("."))
  .aggregate(
    `app-process-api`, `app-process-impl`
  )

lazy val `app-process-api` = (project in file("app-process-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `app-process-impl` = (project in file("app-process-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslAkkaDiscovery,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    ) ++ baseImplDepencencies
  )
  .settings(lagomForkedTestSettings)
  .dependsOn(`app-process-api`)

