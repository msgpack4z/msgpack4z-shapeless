import sbt._, Keys._

object build extends Build {

  private val msgpack4zShapelessName = "msgpack4z-shapeless"
  val modules = msgpack4zShapelessName :: Nil

  lazy val msgpack4z = Project("msgpack4z-shapeless", file(".")).settings(
    Common.settings: _*
  ).settings(
    name := msgpack4zShapelessName,
    libraryDependencies ++= (
      ("com.chuusai" %% "shapeless" % "2.2.5") ::
      ("com.github.xuwei-k" %% "msgpack4z-core" % "0.1.4") ::
      ("com.github.xuwei-k" %% "zeroapply-scalaz" % "0.1.3" % "provided") ::
      ("com.github.alexarchambault" %% "scalacheck-shapeless_1.12" % "0.3.1" % "test") ::
      ("com.github.xuwei-k" % "msgpack4z-java07" % "0.1.5" % "test") ::
      ("com.github.xuwei-k" % "msgpack4z-java06" % "0.1.1" % "test") ::
      ("com.github.xuwei-k" %% "msgpack4z-native" % "0.1.1" % "test") ::
      Nil
    )
  ).settings(
    Sxr.subProjectSxr(Compile, "classes.sxr"): _*
  )

}
