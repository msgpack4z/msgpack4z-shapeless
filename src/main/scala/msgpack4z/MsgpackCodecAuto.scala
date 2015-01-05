package msgpack4z

import shapeless._
import scalaz.{-\/, \/-}

object MsgpackCodecAuto extends TypeClassCompanion[MsgpackCodec] {
  private[msgpack4z] final val HeaderSize = 1

  implicit val MsgpackCodecInstance: TypeClass[MsgpackCodec] =
    new MsgpackCodecTypeClassImpl[Byte](0, 1)(CodecInstances.anyVal.byteCodec)

  def codecTypeClass[A](leftKey: A, rightKey: A)(implicit A: MsgpackCodec[A]): TypeClass[MsgpackCodec] =
    new MsgpackCodecTypeClassImpl(leftKey, rightKey)
}

private final class MsgpackCodecTypeClassImpl[A](LeftKey: A, RightKey: A)(implicit A: MsgpackCodec[A]) extends TypeClass[MsgpackCodec] {

  override def coproduct[L, R <: Coproduct](CL: => MsgpackCodec[L], CR: => MsgpackCodec[R]) = {
    lazy val cl = CL
    lazy val cr = CR
    MsgpackCodec.codec[L :+: R](
      (packer, x) => {
        packer.packMapHeader(MsgpackCodecAuto.HeaderSize)
        x match {
          case Inl(l) =>
            A.pack(packer, LeftKey)
            cl.pack(packer, l)
          case Inr(r) =>
            A.pack(packer, RightKey)
            cr.pack(packer, r)
        }
        packer.mapEnd()
      }
      ,
      unpacker => {
        val size = unpacker.unpackMapHeader()
        if (size == MsgpackCodecAuto.HeaderSize) {
          val result = A.unpack(unpacker).flatMap{
            case LeftKey =>
              cl.unpack(unpacker).map(Inl(_))
            case RightKey =>
              cr.unpack(unpacker).map(Inr(_))
            case other =>
              -\/(new UnexpectedEitherKey(LeftKey, RightKey, other))
          }
          unpacker.mapEnd()
          result
        } else {
          -\/(new UnexpectedMapSize(MsgpackCodecAuto.HeaderSize, size))
        }
      }
    )
  }

  override val emptyCoproduct =
    MsgpackCodec.codec[CNil]((_, _) => (), _ => -\/(Other("impossible")))

  override val emptyProduct =
    MsgpackCodec.tryE[HNil](
      (packer, _) => packer.packNil()
      ,
      unpacker => {
        unpacker.unpackNil()
        \/-(HNil)
      }
    )

  override def product[H, T <: HList](H: MsgpackCodec[H], T: MsgpackCodec[T]) = {
    val ProductSize = 2
    MsgpackCodec.codec(
      (packer, x) => {
        packer.packArrayHeader(ProductSize)
        H.pack(packer, x.head)
        T.pack(packer, x.tail)
        packer.arrayEnd()
      }
      ,
      unpacker => {
        val size = unpacker.unpackArrayHeader
        if (size == ProductSize) {
          val result = zeroapply.DisjunctionApply.apply2(
            H.unpack(unpacker),
            T.unpack(unpacker)
          )(_ :: _)
          unpacker.arrayEnd()
          result
        } else {
          -\/(new UnexpectedArraySize(ProductSize, size))
        }
      }
    )
  }

  override def project[F, G](instance: => MsgpackCodec[G], to: F => G, from: G => F) =
    instance.xmap(from, to)

}
