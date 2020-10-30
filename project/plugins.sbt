scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint:-unused,_"
)

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.4")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.1")
addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.2.10")
