scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint:-unused,_",
)

addSbtPlugin("com.github.sbt" % "sbt-release" % "1.1.0")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.10")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.1.2")
addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.2.24")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.10.0")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.3")
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.6.0")
