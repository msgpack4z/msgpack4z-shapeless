import sbt._, Keys._

object build extends Build {

  private val msgpack4zShapelessName = "msgpack4z-shapeless"
  val modules = msgpack4zShapelessName :: Nil

  private val shapelessContrib = "0.3"

  lazy val msgpack4z = Project("msgpack4z-shapeless", file(".")).settings(
    Common.settings: _*
  ).settings(
    name := msgpack4zShapelessName,
    libraryDependencies ++= (
      ("com.chuusai" %% "shapeless" % "2.0.0") ::
      ("com.github.xuwei-k" %% "msgpack4z-core" % "0.1.3") ::
      ("org.scalaz" %% "scalaz-scalacheck-binding" % "7.1.3" % "test") ::
      ("com.github.xuwei-k" %% "zeroapply-scalaz" % "0.1.2" % "provided") ::
      ("org.typelevel" %% "shapeless-scalaz" % shapelessContrib % "test") ::
      ("org.typelevel" %% "shapeless-scalacheck" % shapelessContrib % "test") ::
      ("com.github.xuwei-k" % "msgpack4z-java07" % "0.1.3" % "test").exclude("org.msgpack", "msgpack-core") ::
      ("org.msgpack" % "msgpack-core" % "0.7.0-p9" % "test") ::
      ("com.github.xuwei-k" % "msgpack4z-java06" % "0.1.0" % "test") ::
      ("com.github.xuwei-k" %% "msgpack4z-native" % "0.1.0" % "test") ::
      Nil
    )
  ).settings(
    Sxr.subProjectSxr(Compile, "classes.sxr"): _*
  )

}
