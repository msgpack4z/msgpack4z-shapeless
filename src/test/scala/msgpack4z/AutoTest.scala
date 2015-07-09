package msgpack4z

import msgpack4z.CodecInstances.all._
import org.scalacheck.Shapeless._
import org.scalacheck.{Arbitrary, Prop, Properties}
import shapeless.TypeClassCompanion
import scala.util.control.NonFatal
import scalaz.std.AllInstances._
import scalaz.{-\/, Equal, \/-}

sealed abstract class AAA[X]
object AAA {
  case class PPP[X]() extends AAA[X]
  final case class BBB[X](a: Byte, b: Option[Float], x: X) extends AAA[X]
  final case class CCC[X](a: Int, b: List[String], c: (String, Byte), x: X) extends AAA[X]
  final case class DDD[X](a: Map[Long, Int], b: (Long, List[Boolean]), x: X) extends AAA[X]
  case class EEE[X]() extends AAA[X]

  // TODO
  implicit def equalInstance[A: Equal]: Equal[AAA[A]] =
    Equal.equalA[AAA[A]]
}

sealed abstract class Tree[A]
final case class Node[A](left: Tree[A], right: Tree[A]) extends Tree[A]
final case class Leaf[A](value: A) extends Tree[A]

object Tree {
  // TODO
  implicit def equalInstance[A: Equal]: Equal[Tree[A]] =
    Equal.equalA[Tree[A]]
}

abstract class SpecBase(typeClassCompanion: TypeClassCompanion[MsgpackCodec], name: String) extends Properties(name) {
  import typeClassCompanion._

  protected[this] def packer(): MsgPacker
  protected[this] def unpacker(bytes: Array[Byte]): MsgUnpacker

  private def checkRoundTripBytes[A](a: A)(implicit A: MsgpackCodec[A], G: Arbitrary[A], E: Equal[A]): Boolean = {
    A.roundtripz(a, packer(), unpacker _) match {
      case None =>
        true
      case Some(\/-(b)) =>
        println("fail roundtrip bytes " + a + " " + b)
        false
      case Some(-\/(e)) =>
        println(e)
        false
    }
  }

  final def checkLaw[A](implicit A: MsgpackCodec[A], G: Arbitrary[A], E: Equal[A]): Prop =
    Prop.forAll{ (a: A) =>
      Prop.secure{
        try {
          checkRoundTripBytes(a)
        }catch{
          case NonFatal(e) =>
            println(a)
            println(e.getStackTrace.map("\tat " + _).mkString("\n" + e.toString + "\n","\n", "\n"))
            throw e
        }
      }
    }

  property("AAA") = checkLaw[AAA[Int]]
  property("Tree") = checkLaw[Tree[Int]]
}

abstract class AutoSpec1(name: String) extends SpecBase(MsgpackCodecAuto, name + " auto1")
abstract class AutoSpec2(name: String) extends SpecBase(MsgpackCodecAuto.typeClassCompanion("foo", "bar"), name + " auto2")

trait Msgpack06Spec{ _: SpecBase =>
  override protected[this] def packer() = Msgpack06.defaultPacker()
  override protected[this] def unpacker(bytes: Array[Byte]) = Msgpack06.defaultUnpacker(bytes)
}

object Msgpack06AutoSpec1 extends AutoSpec1("msgpack06") with Msgpack06Spec
object Msgpack06AutoSpec2 extends AutoSpec2("msgpack06") with Msgpack06Spec


trait Msgpack07Spec{ _: SpecBase =>
  override protected[this] def packer() = new Msgpack07Packer()
  override protected[this] def unpacker(bytes: Array[Byte]) = Msgpack07Unpacker.defaultUnpacker(bytes)
}

object Msgpack07AutoSpec1 extends AutoSpec1("msgpack07") with Msgpack07Spec
object Msgpack07AutoSpec2 extends AutoSpec2("msgpack07") with Msgpack07Spec


trait NativeSpec{ _: SpecBase =>
  override protected[this] def packer() = MsgOutBuffer.create()
  override protected[this] def unpacker(bytes: Array[Byte]) = MsgInBuffer(bytes)
}


object NativeAutoSpec1 extends AutoSpec1("Native") with NativeSpec
object NativeAutoSpec2 extends AutoSpec2("Native") with NativeSpec
