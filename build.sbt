
lazy val root = (project in file(".")).
  settings(
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
