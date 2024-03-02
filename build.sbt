
lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "3.5.0",
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
      "org.typelevel" %% "cats-core" % "2.10.0",
      "org.scalatest" %% "scalatest" % "3.2.17" % "test",
    )

  )
