package com.tripit.flightready.java.nio

import scala.language.higherKinds
import java.nio.ByteOrder

import com.tripit.flightready.{IsoMutableRORW, IsoMutable}
import com.tripit.flightready.integration.category.FlatMap

object BufferIO {
  trait Module[F[_], BIO[_[_]], A] {
    def allocate(capacity: Int): F[BIO[F]]
    def wrapArray(bytes: Array[A]): F[BIO[F]]
    def wrapArraySlice(bytes: Array[A], ofs: Int, len: Int): F[BIO[F]]
  }
}

trait BufferReadIO[F[_], A] {
  // TODO: doc comments and links back
  def capacity: Int // TODO: comment why not functor wrapped

  def isDirect: F[Boolean] // TODO: comment WHY functor wrapped
  def order: F[ByteOrder] // TODO: consider our own ByteOrder

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

trait BufferIO[F[_], A] {
  // TODO: doc comments and links back

  def hasArray: Boolean
  def array: Array[A]
  def arrayOffset: Int

  def compact: F[Unit]

  def put(a: A): F[Unit]
  def putAt(idx: Int, a: A): F[Unit]
  def putArray(as: Array[A]): F[Unit]
  def putArraySlice(as: Array[A], ofs: Int, len: Int): F[Unit]
}


object ByteBufferIO {
  trait Module[F[_]] extends BufferIO.Module[F, ByteBufferIO, Byte] {
    def allocateDirect(capacity: Int): F[ByteBufferIO[F]]
  }
}
object IsoByteBufferIO {
  trait Module[F[_]] extends ByteBufferIO.Module[F] {
    type IORO <: ByteBufferReadIO[F]
    type IORW <: ByteBufferIO[F]

    def isoMutableRORW: IsoMutableRORW[IORO, IORW, java.nio.ByteBuffer]
  }
}

trait ByteBufferReadIO[F[_]] extends BufferReadIO[F, Byte] {
  def duplicateRO: F[ByteBufferReadIO[F]]
  def sliceRO: F[ByteBufferReadIO[F]]

  def asCharBufferRO: F[CharBufferReadIO[F]]
  def asShortBufferRO: F[ShortBufferReadIO[F]]
  def asIntBufferRO: F[IntBufferReadIO[F]]
  def asLongBufferRO: F[LongBufferReadIO[F]]
  def asFloatBufferRO: F[FloatBufferReadIO[F]]
  def asDoubleBufferRO: F[DoubleBufferReadIO[F]]
}

trait ByteBufferIO[F[_]] extends BufferIO[F, Byte] {
  def duplicateRW: F[ByteBufferIO[F]]
  def sliceRW: F[ByteBufferIO[F]]

  def setByteOrder(bo: ByteOrder): F[Unit]

  def asCharBufferRW: F[CharBufferIO[F]]
  def asShortBufferRW: F[ShortBufferIO[F]]
  def asIntBufferRW: F[IntBufferIO[F]]
  def asLongBufferRW: F[LongBufferIO[F]]
  def asFloatBufferRW: F[FloatBufferIO[F]]
  def asDoubleBufferRW: F[DoubleBufferIO[F]]

  def putBuffer(buf: ByteBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit]

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
  trait Module[F[_]] extends BufferIO.Module[F, CharBufferIO, Char] {
    def wrapCharSequence(csq: CharSequence): F[CharBufferReadIO[F]]
    def wrapCharSequenceSlice(csq: CharSequence, start: Int, end: Int): F[CharBufferReadIO[F]]
  }
}
object IsoCharBufferIO {
  trait Module[F[_]] extends CharBufferIO.Module[F] {
    type IORO <: CharBufferReadIO[F]
    type IORW <: CharBufferIO[F]

    def isoMutableRORW: IsoMutableRORW[IORO, IORW, java.nio.CharBuffer]
  }
}

trait CharBufferReadIO[F[_]] extends BufferReadIO[F, Char] {
  def duplicateRO: F[CharBufferReadIO[F]]
  def sliceRO: F[CharBufferReadIO[F]]

  def charAt(idx: Int): F[Char]
  def read(dst: CharBufferIO[F])(implicit fm: FlatMap[F]): F[Int]
  def subSequenceRO(start: Int, end: Int): F[CharBufferReadIO[F]]
}

// eliding the "append" methods because their reliance on csq.toString is just comically terrible
trait CharBufferIO[F[_]] extends BufferIO[F, Char] {
  def duplicateRW: F[CharBufferIO[F]]
  def sliceRW: F[CharBufferIO[F]]

  def putBuffer(buf: CharBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit]
  def subSequenceRW(start: Int, end: Int): F[CharBufferIO[F]]

  def putString(s: String): F[Unit]
  def putStringSlice(s: String, start: Int, end: Int): F[Unit]
}


object ShortBufferIO {
  trait Module[F[_]] extends BufferIO.Module[F, ShortBufferIO, Short]
}
object IsoShortBufferIO {
  trait Module[F[_]] extends ShortBufferIO.Module[F] {
    type IORO <: ShortBufferReadIO[F]
    type IORW <: ShortBufferIO[F]

    def isoMutableRORW: IsoMutableRORW[IORO, IORW, java.nio.ShortBuffer]
  }
}

trait ShortBufferReadIO[F[_]] extends BufferReadIO[F, Short] {
  def duplicateRO: F[ShortBufferReadIO[F]]
  def sliceRO: F[ShortBufferReadIO[F]]
}
trait ShortBufferIO[F[_]] extends BufferIO[F, Short] {
  def duplicateRW: F[ShortBufferIO[F]]
  def sliceRW: F[ShortBufferIO[F]]

  def putBuffer(buf: ShortBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit]
}


object IntBufferIO {
  trait Module[F[_]] extends BufferIO.Module[F, IntBufferIO, Int]
}
object IsoIntBufferIO {
  trait Module[F[_]] extends IntBufferIO.Module[F] {
    type IORO <: IntBufferReadIO[F]
    type IORW <: IntBufferIO[F]

    def isoMutableRORW: IsoMutableRORW[IORO, IORW, java.nio.IntBuffer]
  }
}

trait IntBufferReadIO[F[_]] extends BufferReadIO[F,  Int] {
  def duplicateRO: F[IntBufferReadIO[F]]
  def sliceRO: F[IntBufferReadIO[F]]
}
trait IntBufferIO[F[_]] extends BufferIO[F, Int] {
  def duplicateRW: F[IntBufferIO[F]]
  def sliceRW: F[IntBufferIO[F]]

  def putBuffer(buf: IntBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit]
}


object LongBufferIO {
  trait Module[F[_]] extends BufferIO.Module[F, LongBufferIO, Long]
}
object IsoLongBufferIO {
  trait Module[F[_]] extends LongBufferIO.Module[F] {
    type IORO <: LongBufferReadIO[F]
    type IORW <: LongBufferIO[F]

    def isoMutable: IsoMutableRORW[IORO, IORW, java.nio.LongBuffer]
  }
}

trait LongBufferReadIO[F[_]] extends BufferReadIO[F, Long] {
  def duplicateRO: F[LongBufferReadIO[F]]
  def sliceRO: F[LongBufferReadIO[F]]
}
trait LongBufferIO[F[_]] extends BufferIO[F, Long] {
  def duplicateRW: F[LongBufferIO[F]]
  def sliceRW: F[LongBufferIO[F]]

  def putBuffer(buf: LongBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit]
}


object FloatBufferIO {
  trait Module[F[_]] extends BufferIO.Module[F, FloatBufferIO, Float]
}
object IsoFloatBufferIO {
  trait Module[F[_]] extends FloatBufferIO.Module[F] {
    type IORO <: FloatBufferReadIO[F]
    type IORW <: FloatBufferIO[F]

    def isoMutable: IsoMutableRORW[IORO, IORW, java.nio.FloatBuffer]
  }
}

trait FloatBufferReadIO[F[_]] extends BufferReadIO[F, Float] {
  def duplicateRO: F[FloatBufferReadIO[F]]
  def sliceRO: F[FloatBufferReadIO[F]]
}
trait FloatBufferIO[F[_]] extends FloatBufferReadIO[F] with BufferIO[F, Float] {
  def duplicateRW: F[FloatBufferIO[F]]
  def sliceRW: F[FloatBufferIO[F]]

  def putBuffer(buf: FloatBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit]
}


object DoubleBufferIO {
  trait Module[F[_]] extends BufferIO.Module[F, DoubleBufferIO, Double]
}
object IsoDoubleBufferIO {
  trait Module[F[_]] extends DoubleBufferIO.Module[F] {
    type IORO <: DoubleBufferReadIO[F]
    type IORW <: DoubleBufferIO[F]

    def isoMutable: IsoMutableRORW[IORO, IORW, java.nio.DoubleBuffer]
  }
}

trait DoubleBufferReadIO[F[_]] extends BufferReadIO[F, Double] {
  def duplicateRO: F[DoubleBufferReadIO[F]]
  def sliceRO: F[DoubleBufferReadIO[F]]
}

trait DoubleBufferIO[F[_]] extends DoubleBufferReadIO[F] with BufferIO[F, Double] {
  def duplicateRW: F[DoubleBufferIO[F]]
  def sliceRW: F[DoubleBufferIO[F]]

  // TODO: comment on why the Monad
  def putBuffer(buf: DoubleBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit]
}


