sbtPlugin := true

name := "sbt-jooq"
description := "jOOQ plugin for SBT 0.13.5+"
organization := "com.github.kxbmap"

crossSbtVersions := Seq("1.0.4", "0.13.16")

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint"
)

libraryDependencies ++= {
  (sbtBinaryVersion in pluginCrossBuild).value match {
    case "0.13" => Seq(
      "com.lihaoyi" %% "fastparse" % "1.0.0",
      compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
    )
    case _ => Nil
  }
}
