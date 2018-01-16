package flightready.java.nio

import java.nio.ByteOrder

import flightready.IsoMutableRORW

trait BufferReadIO[F[_], A] {
  def capacity: F[Int]

  def isDirect: F[Boolean]
  def order: F[ByteOrder]

  def limit: F[Int]
  def mark: F[Unit]
  def position: F[Int]
  def hasRemaining: F[Boolean]
  def remaining: F[Int]

  def setLimit(limit: Int): F[Unit]
  def setPosition(position: Int): F[Unit]

  def clear: F[Unit]
  def flip: F[Unit]
  def reset: F[Unit]
  def rewind: F[Unit]

  def get: F[A]
  def getAt(idx: Int): F[A]
  def getInto(dst: Array[A]): F[Unit]
  def getIntoSlice(dst: Array[A], ofs: Int, len: Int): F[Unit]
}

trait BufferIO[F[_], A] extends BufferReadIO[F, A] {
  def hasArray: F[Boolean]
  def array: F[Array[A]]
  def arrayOffset: F[Int]

  def compact: F[Unit]

  def put(a: A): F[Unit]
  def putAt(idx: Int, a: A): F[Unit]
  def putArray(as: Array[A]): F[Unit]
  def putArraySlice(as: Array[A], ofs: Int, len: Int): F[Unit]
}

object BufferIO {
  trait Module[F[_]] {
    type ByteBufMod <: ByteBufferIO.Module[F]
    type CharBufMod <: CharBufferIO.Module[F]
    type ShortBufMod <: ShortBufferIO.Module[F]
    type IntBufMod <: IntBufferIO.Module[F]
    type LongBufMod <: LongBufferIO.Module[F]
    type FloatBufMod <: FloatBufferIO.Module[F]
    type DoubleBufMod <: DoubleBufferIO.Module[F]

    def byteBufferModule: ByteBufMod
    def charBufferModule: CharBufMod
    def shortBufferModule: ShortBufMod
    def intBufferModule: IntBufMod
    def longBufferModule: LongBufMod
    def floatBufferModule: FloatBufMod
    def doubleBufferModule: DoubleBufMod
  }
}

object ByteBufferIO {
  trait Module[F[_]] {
    type IORO <: ByteBufferReadIO[F, _ <: BufferIO.Module[F]]
    type IORW <: ByteBufferIO[F, _ <: BufferIO.Module[F]]

    def allocate(capacity: Int): F[IORW]
    def allocateDirect(capacity: Int): F[IORW]
    def wrapArray(bytes: Array[Byte]): F[IORW]
    def wrapArraySlice(bytes: Array[Byte], ofs: Int, len: Int): F[IORW]
  }
}
object IsoByteBufferIO {
  trait Module[F[_]] extends ByteBufferIO.Module[F] {
    def isoMutableRORW: IsoMutableRORW[IORO, IORW, java.nio.ByteBuffer]
  }
}

trait ByteBufferReadIO[F[_], BufMod <: BufferIO.Module[F]] extends BufferReadIO[F, Byte] {
  def duplicateRO: F[BufMod#ByteBufMod#IORO]
  def sliceRO: F[BufMod#ByteBufMod#IORO]

  def asCharBufferRO: F[BufMod#CharBufMod#IORO]
  def asShortBufferRO: F[BufMod#ShortBufMod#IORO]
  def asIntBufferRO: F[BufMod#IntBufMod#IORO]
  def asLongBufferRO: F[BufMod#LongBufMod#IORO]
  def asFloatBufferRO: F[BufMod#FloatBufMod#IORO]
  def asDoubleBufferRO: F[BufMod#DoubleBufMod#IORO]
}

trait ByteBufferIO[F[_], BufMod <: BufferIO.Module[F]] extends BufferIO[F, Byte] with ByteBufferReadIO[F, BufMod] {
  def duplicateRW: F[BufMod#ByteBufMod#IORW]
  def sliceRW: F[BufMod#ByteBufMod#IORW]

  def setByteOrder(bo: ByteOrder): F[Unit]

  def asCharBufferRW: F[BufMod#CharBufMod#IORW]
  def asShortBufferRW: F[BufMod#ShortBufMod#IORW]
  def asIntBufferRW: F[BufMod#IntBufMod#IORW]
  def asLongBufferRW: F[BufMod#LongBufMod#IORW]
  def asFloatBufferRW: F[BufMod#FloatBufMod#IORW]
  def asDoubleBufferRW: F[BufMod#DoubleBufMod#IORW]

  def putBuffer(buf: BufMod#ByteBufMod#IORO): F[Unit]

  def putChar(c: Char): F[Unit]
  def putCharAt(idx: Int, c: Char): F[Unit]
  def putShort(s: Short): F[Unit]
  def putShortAt(idx: Int, s: Short): F[Unit]
  def putInt(i: Int): F[Unit]
  def putIntAt(idx: Int, i: Int): F[Unit]
  def putLong(l: Long): F[Unit]
  def putLongAt(idx: Int, l: Long): F[Unit]
  def putFloat(f: Float): F[Unit]
  def putFloatAt(idx: Int, f: Float): F[Unit]
  def putDouble(d: Double): F[Unit]
  def putDoubleAt(idx: Int, d: Double): F[Unit]
}


object CharBufferIO {
  trait Module[F[_]] {
    type IORO <: CharBufferReadIO[F, _ <: CharBufferIO.Module[F]]
    type IORW <: CharBufferIO[F, _ <: CharBufferIO.Module[F]]

    def allocate(capacity: Int): F[IORW]

    def wrapArray(chars: Array[Char]): F[IORW]
    def wrapArraySlice(chars: Array[Char], ofs: Int, len: Int): F[IORW]

    def wrapCharSequence(csq: CharSequence): F[IORO]
    def wrapCharSequenceSlice(csq: CharSequence, start: Int, end: Int): F[IORO]
  }
}
object IsoCharBufferIO {
  trait Module[F[_]] extends CharBufferIO.Module[F] {
    def isoMutableRORW: IsoMutableRORW[IORO, IORW, java.nio.CharBuffer]
  }
}

trait CharBufferReadIO[F[_], CharBufMod <: CharBufferIO.Module[F]] extends BufferReadIO[F, Char] {
  def duplicateRO: F[CharBufMod#IORO]
  def sliceRO: F[CharBufMod#IORO]

  def charAt(idx: Int): F[Char]
  def read(dst: CharBufMod#IORW): F[Int]
  def subSequenceRO(start: Int, end: Int): F[CharBufMod#IORO]
}

// eliding the "append" methods because their reliance on csq.toString is just comically terrible
trait CharBufferIO[F[_], CharBufMod <: CharBufferIO.Module[F]] 
    extends BufferIO[F, Char] with CharBufferReadIO[F, CharBufMod] {

  def duplicateRW: F[CharBufMod#IORW]
  def sliceRW: F[CharBufMod#IORW]

  def putBuffer(buf: CharBufMod#IORO): F[Unit]
  def subSequenceRW(start: Int, end: Int): F[CharBufMod#IORW]

  def putString(s: String): F[Unit]
  def putStringSlice(s: String, start: Int, end: Int): F[Unit]
}


object ShortBufferIO {
  trait Module[F[_]] {
    type IORO <: ShortBufferReadIO[F, _ <: ShortBufferIO.Module[F]]
    type IORW <: ShortBufferIO[F, _ <: ShortBufferIO.Module[F]]

    def allocate(capacity: Int): F[IORW]

    def wrapArray(shorts: Array[Short]): F[IORW]
    def wrapArraySlice(shorts: Array[Short], ofs: Int, len: Int): F[IORW]
  }
}
object IsoShortBufferIO {
  trait Module[F[_]] extends ShortBufferIO.Module[F] {
    def isoMutableRORW: IsoMutableRORW[IORO, IORW, java.nio.ShortBuffer]
  }
}

trait ShortBufferReadIO[F[_], ShortBufMod <: ShortBufferIO.Module[F]] extends BufferReadIO[F, Short] {
  def duplicateRO: F[ShortBufMod#IORO]
  def sliceRO: F[ShortBufMod#IORO]
}
trait ShortBufferIO[F[_], ShortBufMod <: ShortBufferIO.Module[F]] 
    extends BufferIO[F, Short] with ShortBufferReadIO[F, ShortBufMod] {

  def duplicateRW: F[ShortBufMod#IORW]
  def sliceRW: F[ShortBufMod#IORW]

  def putBuffer(buf: ShortBufMod#IORO): F[Unit]
}


object IntBufferIO {
  trait Module[F[_]] {
    type IORO <: IntBufferReadIO[F, _ <: IntBufferIO.Module[F]]
    type IORW <: IntBufferIO[F, _ <: IntBufferIO.Module[F]]

    def allocate(capacity: Int): F[IORW]

    def wrapArray(ints: Array[Int]): F[IORW]
    def wrapArraySlice(ints: Array[Int], ofs: Int, len: Int): F[IORW]
  }
}
object IsoIntBufferIO {
  trait Module[F[_]] extends IntBufferIO.Module[F] {
    def isoMutableRORW: IsoMutableRORW[IORO, IORW, java.nio.IntBuffer]
  }
}

trait IntBufferReadIO[F[_], IntBufMod <: IntBufferIO.Module[F]] extends BufferReadIO[F,  Int] {
  def duplicateRO: F[IntBufMod#IORO]
  def sliceRO: F[IntBufMod#IORO]
}
trait IntBufferIO[F[_], IntBufMod <: IntBufferIO.Module[F]] 
    extends BufferIO[F, Int] with IntBufferReadIO[F, IntBufMod] {

  def duplicateRW: F[IntBufMod#IORW]
  def sliceRW: F[IntBufMod#IORW]

  def putBuffer(buf: IntBufMod#IORO): F[Unit]
}


object LongBufferIO {
  trait Module[F[_]] {
    type IORO <: LongBufferReadIO[F, _ <: LongBufferIO.Module[F]]
    type IORW <: LongBufferIO[F, _ <: LongBufferIO.Module[F]]

    def allocate(capacity: Int): F[IORW]

    def wrapArray(longs: Array[Long]): F[IORW]
    def wrapArraySlice(longs: Array[Long], ofs: Int, len: Int): F[IORW]
  }
}
object IsoLongBufferIO {
  trait Module[F[_]] extends LongBufferIO.Module[F] {
    def isoMutable: IsoMutableRORW[IORO, IORW, java.nio.LongBuffer]
  }
}

trait LongBufferReadIO[F[_], LongBufMod <: LongBufferIO.Module[F]] extends BufferReadIO[F, Long] {
  def duplicateRO: F[LongBufMod#IORO]
  def sliceRO: F[LongBufMod#IORO]
}
trait LongBufferIO[F[_], LongBufMod <: LongBufferIO.Module[F]] 
    extends BufferIO[F, Long] with LongBufferReadIO[F, LongBufMod] {

  def duplicateRW: F[LongBufMod#IORW]
  def sliceRW: F[LongBufMod#IORW]

  def putBuffer(buf: LongBufMod#IORO): F[Unit]
}


object FloatBufferIO {
  trait Module[F[_]] {
    type IORO <: FloatBufferReadIO[F, _ <: FloatBufferIO.Module[F]]
    type IORW <: FloatBufferIO[F, _ <: FloatBufferIO.Module[F]]

    def allocate(capacity: Int): F[IORW]

    def wrapArray(floats: Array[Float]): F[IORW]
    def wrapArraySlice(floats: Array[Float], ofs: Int, len: Int): F[IORW]
  }
}
object IsoFloatBufferIO {
  trait Module[F[_]] extends FloatBufferIO.Module[F] {
    def isoMutable: IsoMutableRORW[IORO, IORW, java.nio.FloatBuffer]
  }
}

trait FloatBufferReadIO[F[_], FloatBufMod <: FloatBufferIO.Module[F]] extends BufferReadIO[F, Float] {
  def duplicateRO: F[FloatBufMod#IORO]
  def sliceRO: F[FloatBufMod#IORO]
}
trait FloatBufferIO[F[_], FloatBufMod <: FloatBufferIO.Module[F]] 
    extends BufferIO[F, Float] with FloatBufferReadIO[F, FloatBufMod] {

  def duplicateRW: F[FloatBufMod#IORW]
  def sliceRW: F[FloatBufMod#IORW]

  def putBuffer(buf: FloatBufMod#IORO): F[Unit]
}


object DoubleBufferIO {
  trait Module[F[_]] {
    type IORO <: DoubleBufferReadIO[F, _ <: DoubleBufferIO.Module[F]]
    type IORW <: DoubleBufferIO[F, _ <: DoubleBufferIO.Module[F]]

    def allocate(capacity: Int): F[IORW]

    def wrapArray(doubles: Array[Double]): F[IORW]
    def wrapArraySlice(doubles: Array[Double], ofs: Int, len: Int): F[IORW]
  }
}
object IsoDoubleBufferIO {
  trait Module[F[_]] extends DoubleBufferIO.Module[F] {
    def isoMutable: IsoMutableRORW[IORO, IORW, java.nio.DoubleBuffer]
  }
}

trait DoubleBufferReadIO[F[_], DoubleBufMod <: DoubleBufferIO.Module[F]] extends BufferReadIO[F, Double] {
  def duplicateRO: F[DoubleBufMod#IORO]
  def sliceRO: F[DoubleBufMod#IORO]
}
trait DoubleBufferIO[F[_], DoubleBufMod <: DoubleBufferIO.Module[F]] 
    extends BufferIO[F, Double] with DoubleBufferReadIO[F, DoubleBufMod] {

  def duplicateRW: F[DoubleBufMod#IORW]
  def sliceRW: F[DoubleBufMod#IORW]

  def putBuffer(buf: DoubleBufMod#IORO): F[Unit]
}
