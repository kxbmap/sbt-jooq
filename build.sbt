sbtPlugin := true

name := "sbt-jooq"
description := "jOOQ plugin for SBT 0.13.5+"
organization := "com.github.kxbmap"

crossSbtVersions := Seq("1.1.6", "0.13.17")

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint"
)

addSbtPlugin("com.github.kxbmap" % "sbt-slf4j-simple" % "0.1.0")

libraryDependencies ++= {
  (sbtBinaryVersion in pluginCrossBuild).value match {
    case "0.13" => Seq(
      "com.lihaoyi" %% "fastparse" % "1.0.0",
      compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
    )
    case _ => Nil
  }
}
