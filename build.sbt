
lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "3.3.0",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "validation-scala3",
    version := "0.1.0",
    scalaVersion := "3.3.0",
    scalacOptions += "-Ykind-projector",
    scalacOptions += "-Xcheck-macros",
    scalacOptions += "-Wvalue-discard",
    resolvers += Resolver.sonatypeRepo("snapshots"),
    resolvers += Resolver.mavenLocal,


    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.5.0",
      "org.typelevel" %% "cats-mtl" % "1.3.0",
      "com.github.pureconfig" %% "pureconfig-cats" % "0.17.2",
      "ch.qos.logback" % "logback-classic" % "1.4.6",
      "io.github.iltotore" %% "iron" % "2.0.0",
      "org.scalatest" %% "scalatest" % "3.2.15" % "test",
      "org.typelevel" %% "cats-effect-testing-scalatest" % "1.4.0" % Test
    )

  )
