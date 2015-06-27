addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.1")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.1")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "0.4.0")

scalacOptions ++= (
  "-deprecation" ::
  "-unchecked" ::
  "-Xlint" ::
  "-optimize" ::
  "-language:existentials" ::
  "-language:higherKinds" ::
  "-language:implicitConversions" ::
  Nil
)
