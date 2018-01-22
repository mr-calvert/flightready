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
    // TODO: kill these
    type IORO <: ByteBufferReadIO[F, _ <: BufferIO.Module[F]]
    type IORW <: ByteBufferIO[F, _ <: BufferIO.Module[F]]

    type A

    def allocate(capacity: Int): F[IORW]
    def allocateDirect(capacity: Int): F[IORW]
    def wrapArray(bytes: Array[Byte]): F[IORW]
    def wrapArraySlice(bytes: Array[Byte], ofs: Int, len: Int): F[IORW]
  }
}
object IsoByteBufferIO {
  trait Module[F[_]] extends ByteBufferIO.Module[F] {
    def isoMutableRORW: IsoMutableRORW[ByteBufferReadIO[F, A], ByteBufferIO[F, A], java.nio.ByteBuffer]
  }
}

trait ByteBufferReadIO[F[_], A] extends BufferReadIO[F, Byte] {
  def duplicateRO: F[ByteBufferReadIO[F, A]]
  def sliceRO: F[ByteBufferReadIO[F, A]]

  def asCharBufferRO: F[CharBufferReadIO[F, A]]
  def asShortBufferRO: F[ShortBufferReadIO[F, A]]
  def asIntBufferRO: F[IntBufferReadIO[F, A]]
  def asLongBufferRO: F[LongBufferReadIO[F, A]]
  def asFloatBufferRO: F[FloatBufferReadIO[F, A]]
  def asDoubleBufferRO: F[DoubleBufferReadIO[F, A]]
}

trait ByteBufferIO[F[_], A] extends BufferIO[F, Byte] with ByteBufferReadIO[F, A] {
  def duplicateRW: F[ByteBufferIO[F, A]]
  def sliceRW: F[ByteBufferIO[F, A]]

  def setByteOrder(bo: ByteOrder): F[Unit]

  def asCharBufferRW: F[CharBufferIO[F, A]]
  def asShortBufferRW: F[ShortBufferIO[F, A]]
  def asIntBufferRW: F[IntBufferIO[F, A]]
  def asLongBufferRW: F[LongBufferIO[F, A]]
  def asFloatBufferRW: F[FloatBufferIO[F, A]]
  def asDoubleBufferRW: F[DoubleBufferIO[F, A]]

  def putBuffer(buf: ByteBufferReadIO[F, A]): F[Unit]

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
    type A

    // TODO: kill these
    type IORO <: CharBufferReadIO[F, A]
    type IORW <: CharBufferIO[F, A]

    def allocate(capacity: Int): F[IORW]

    def wrapArray(chars: Array[Char]): F[IORW]
    def wrapArraySlice(chars: Array[Char], ofs: Int, len: Int): F[IORW]

    def wrapCharSequence(csq: CharSequence): F[IORO]
    def wrapCharSequenceSlice(csq: CharSequence, start: Int, end: Int): F[IORO]
  }
}
object IsoCharBufferIO {
  trait Module[F[_]] extends CharBufferIO.Module[F] {
    def isoMutableRORW: IsoMutableRORW[CharBufferReadIO[F, A], CharBufferIO[F, A], java.nio.CharBuffer]
  }
}

trait CharBufferReadIO[F[_], A] extends BufferReadIO[F, Char] {
  def duplicateRO: F[CharBufferReadIO[F, A]]
  def sliceRO: F[CharBufferReadIO[F, A]]

  def charAt(idx: Int): F[Char]
  def read(dst: CharBufferIO[F, A]): F[Int]
  def subSequenceRO(start: Int, end: Int): F[CharBufferReadIO[F, A]]
}

// eliding the "append" methods because their reliance on csq.toString is just comically terrible
trait CharBufferIO[F[_], A] extends BufferIO[F, Char] with CharBufferReadIO[F, A] {
  def duplicateRW: F[CharBufferIO[F, A]]
  def sliceRW: F[CharBufferIO[F, A]]

  def putBuffer(buf: CharBufferReadIO[F, A]): F[Unit]
  def subSequenceRW(start: Int, end: Int): F[CharBufferIO[F, A]]

  def putString(s: String): F[Unit]
  def putStringSlice(s: String, start: Int, end: Int): F[Unit]
}


object ShortBufferIO {
  trait Module[F[_]] {
    type A

    // TODO: kill these
    type IORO <: ShortBufferReadIO[F, A]
    type IORW <: ShortBufferIO[F, A]

    def allocate(capacity: Int): F[IORW]

    def wrapArray(shorts: Array[Short]): F[IORW]
    def wrapArraySlice(shorts: Array[Short], ofs: Int, len: Int): F[IORW]
  }
}
object IsoShortBufferIO {
  trait Module[F[_]] extends ShortBufferIO.Module[F] {
    def isoMutableRORW: IsoMutableRORW[ShortBufferReadIO[F, A], ShortBufferIO[F, A], java.nio.ShortBuffer]
  }
}

trait ShortBufferReadIO[F[_], A] extends BufferReadIO[F, Short] {
  def duplicateRO: F[ShortBufferReadIO[F, A]]
  def sliceRO: F[ShortBufferReadIO[F, A]]
}
trait ShortBufferIO[F[_], A] extends BufferIO[F, Short] with ShortBufferReadIO[F, A] {
  def duplicateRW: F[ShortBufferIO[F, A]]
  def sliceRW: F[ShortBufferIO[F, A]]

  def putBuffer(buf: ShortBufferReadIO[F, A]): F[Unit]
}


object IntBufferIO {
  trait Module[F[_]] {
    type A
    type IORO <: IntBufferReadIO[F, A]
    type IORW <: IntBufferIO[F, A]

    def allocate(capacity: Int): F[IORW]

    def wrapArray(ints: Array[Int]): F[IORW]
    def wrapArraySlice(ints: Array[Int], ofs: Int, len: Int): F[IORW]
  }
}
object IsoIntBufferIO {
  trait Module[F[_]] extends IntBufferIO.Module[F] {
    def isoMutableRORW: IsoMutableRORW[IntBufferReadIO[F, A], IntBufferIO[F, A], java.nio.IntBuffer]
  }
}

trait IntBufferReadIO[F[_], A] extends BufferReadIO[F,  Int] {
  def duplicateRO: F[IntBufferReadIO[F, A]]
  def sliceRO: F[IntBufferReadIO[F, A]]
}
trait IntBufferIO[F[_], A] extends BufferIO[F, Int] with IntBufferReadIO[F, A] {
  def duplicateRW: F[IntBufferIO[F, A]]
  def sliceRW: F[IntBufferIO[F, A]]

  def putBuffer(buf: IntBufferReadIO[F, A]): F[Unit]
}


object LongBufferIO {
  trait Module[F[_]] {
    type A

    type IORO <: LongBufferReadIO[F, A]
    type IORW <: LongBufferIO[F, A]

    def allocate(capacity: Int): F[IORW]

    def wrapArray(longs: Array[Long]): F[IORW]
    def wrapArraySlice(longs: Array[Long], ofs: Int, len: Int): F[IORW]
  }
}
object IsoLongBufferIO {
  trait Module[F[_]] extends LongBufferIO.Module[F] {
    def isoMutableRORW: IsoMutableRORW[LongBufferReadIO[F, JVMBufferIOModule[F]], 
                                   LongBufferIO[F, JVMBufferIOModule[F]], 
                                   java.nio.LongBuffer]
  }
}

trait LongBufferReadIO[F[_], A] extends BufferReadIO[F, Long] {
  def duplicateRO: F[LongBufferReadIO[F, A]]
  def sliceRO: F[LongBufferReadIO[F, A]]
}
trait LongBufferIO[F[_], A] extends BufferIO[F, Long] with LongBufferReadIO[F, A] {
  def duplicateRW: F[LongBufferIO[F, A]]
  def sliceRW: F[LongBufferIO[F, A]]

  def putBuffer(buf: LongBufferReadIO[F, A]): F[Unit]
}


object FloatBufferIO {
  trait Module[F[_]] {
    type A

    type IORO <: FloatBufferReadIO[F, A]
    type IORW <: FloatBufferIO[F, A]

    def allocate(capacity: Int): F[IORW]

    def wrapArray(floats: Array[Float]): F[IORW]
    def wrapArraySlice(floats: Array[Float], ofs: Int, len: Int): F[IORW]
  }
}
object IsoFloatBufferIO {
  trait Module[F[_]] extends FloatBufferIO.Module[F] {
    def isoMutableRORW: IsoMutableRORW[FloatBufferReadIO[F, JVMBufferIOModule[F]], 
                                       FloatBufferIO[F, JVMBufferIOModule[F]] , 
                                       java.nio.FloatBuffer]
  }
}

trait FloatBufferReadIO[F[_], A] extends BufferReadIO[F, Float] {
  def duplicateRO: F[FloatBufferReadIO[F, A]]
  def sliceRO: F[FloatBufferReadIO[F, A]]
}
trait FloatBufferIO[F[_], A] extends BufferIO[F, Float] with FloatBufferReadIO[F, A] {
  def duplicateRW: F[FloatBufferIO[F, A]]
  def sliceRW: F[FloatBufferIO[F, A]]

  def putBuffer(buf: FloatBufferReadIO[F, A]): F[Unit]
}


object DoubleBufferIO {
  trait Module[F[_]] {
    type A

    type IORO <: DoubleBufferReadIO[F, A]
    type IORW <: DoubleBufferIO[F, A]

    def allocate(capacity: Int): F[IORW]

    def wrapArray(doubles: Array[Double]): F[IORW]
    def wrapArraySlice(doubles: Array[Double], ofs: Int, len: Int): F[IORW]
  }
}
object IsoDoubleBufferIO {
  trait Module[F[_]] extends DoubleBufferIO.Module[F] {
    def isoMutableRORW: IsoMutableRORW[DoubleBufferReadIO[F, A], DoubleBufferIO[F, A], java.nio.DoubleBuffer]
  }
}

trait DoubleBufferReadIO[F[_], A] extends BufferReadIO[F, Double] {
  def duplicateRO: F[DoubleBufferReadIO[F, A]]
  def sliceRO: F[DoubleBufferReadIO[F, A]]
}
trait DoubleBufferIO[F[_], A] extends BufferIO[F, Double] with DoubleBufferReadIO[F, A] {
  def duplicateRW: F[DoubleBufferIO[F, A]]
  def sliceRW: F[DoubleBufferIO[F, A]]

  def putBuffer(buf: DoubleBufferReadIO[F, A]): F[Unit]
}
