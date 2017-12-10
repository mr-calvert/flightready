package com.tripit.flightready.java.nio.file

import scala.language.higherKinds

import java.nio.ByteOrder

import com.tripit.flightready.IsoMutable

trait BufferReadIO[F[_], BIO[_[_]], A] { self: BIO[F] =>
  // TODO: doc comments and links back
  def isDirect: F[Boolean]
  def order: F[ByteOrder] // TODO: consider our own ByteOrder

  def capacity: F[Int]
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

  def duplicate: BIO[F]
  def slice: BIO[F]

  def get: F[A]
  def getAt(idx: Int): F[A]
  def getInto(dst: Array[A]): F[Unit]
  def getIntoSlice(dst: Array[A], ofs: Int, len: Int): F[Unit]
}

trait BufferIO[F[_], BIO[_[_]], A] { self: BIO[F] =>
  // TODO: doc comments and links back
  def hasArray: F[Boolean]
  def array: F[Array[A]]
  def arrayOffset: F[Int]

  def duplicate: BIO[F]

  def compact: F[Unit]

  def put(a: A): F[Unit]
  def putBuffer(db: BIO[F])
  def putFrom(as: Array[A]): F[Unit]
  def putFromSlice(as: Array[A], ofs: Int, len: Int): F[Unit]
  def putAt(idx: Int, a: A): F[Unit]
}

object BufferIO {
  trait Module[F[_], BIO[_[_]]] {
    def allocate(capacity: Int): F[BIO[F]]
    def allocateDirect(capacity: Int): F[BIO[F]]

    def wrap(bytes: Array[Byte]): BIO[F]
    def wrap(bytes: Array[Byte], ofs: Int, len: Int): BIO[F]
  }
}


object ByteBufferIO {
  trait Module[F[_]] extends BufferIO.Module[F, ByteBufferIO]
}
object IsoByteBufferIO {
  trait Module[F[_]] extends ByteBufferIO.Module[F] with IsoMutable[ByteBufferIO, F, java.nio.ByteBuffer]
}

trait ByteBufferReadIO[F[_]] extends BufferReadIO[F, ByteBufferReadIO, Byte] {
  def asCharBuffer: CharBufferReadIO[F]
  def asShortBuffer: ShortBufferReadIO[F]
  def asIntBuffer: IntBufferReadIO[F]
  def asLongBuffer: LongBufferReadIO[F]
  def asFloatBuffer: FloatBufferReadIO[F]
  def asDoubleBuffer: DoubleBufferReadIO[F]

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

trait ByteBufferIO[F[_]] extends BufferIO[F, ByteBufferIO, Byte] {
  def asReadOnly: ByteBufferIO[F]
  def asCharBuffer: CharBufferIO[F]
  def asShortBuffer: ShortBufferIO[F]
  def asIntBuffer: IntBufferIO[F]
  def asLongBuffer: LongBufferIO[F]
  def asFloatBuffer: FloatBufferIO[F]
  def asDoubleBuffer: DoubleBufferIO[F]
}


object CharBufferIO {
  trait Module[F[_]] extends BufferIO.Module[F, CharBufferIO] {
    def wrap(csq: CharSequence): CharBufferReadIO[F]
    def wrap(csq: CharSequence, start: Int, end: Int): CharBufferReadIO[F]
  }
}
object IsoCharBufferIO {
  trait Module[F[_]] extends CharBufferIO.Module[F] with IsoMutable[CharBufferIO, F, java.nio.CharBuffer]
}

trait CharBufferReadIO[F[_]] extends BufferReadIO[F, CharBufferReadIO, Char] {
  def charAt(idx: Int): F[Char]
  def read(dst: CharBufferIO[F]): F[Int]
  def subSequence(start: Int, end: Int): F[CharBufferReadIO[F]]
}

// eliding the "append" methods because their reliance on csq.toString is just comically terrible
trait CharBufferIO[F[_]] extends BufferIO[F, CharBufferIO, Char] {
  def asReadOnly: CharBufferReadIO[F]

  def subSequence(start: Int, end: Int): F[CharBufferIO[F]]

  def putString(s: String): F[Unit]
  def putStringSlice(s: String, start: Int, end: Int): F[Unit]
}


object ShortBufferIO {
  trait Module[F[_]] extends BufferIO.Module[F, ShortBufferIO]
}
object IsoShortBufferIO {
  trait Module[F[_]] extends ShortBufferIO.Module[F] with IsoMutable[ShortBufferIO, F, java.nio.ShortBuffer]
}

trait ShortBufferReadIO[F[_]] extends BufferReadIO[F, ShortBufferReadIO, Short]
trait ShortBufferIO[F[_]] extends BufferIO[F, ShortBufferIO, Short] {
  def asReadOnly: ShortBufferReadIO[F]
}


object IntBufferIO {
  trait Module[F[_]] extends BufferIO.Module[F, IntBufferIO]
}
object IsoIntBufferIO {
  trait Module[F[_]] extends IntBufferIO.Module[F] with IsoMutable[IntBufferIO, F, java.nio.IntBuffer]
}

trait IntBufferReadIO[F[_]] extends BufferReadIO[F, IntBufferReadIO, Int]
trait IntBufferIO[F[_]] extends BufferIO[F, IntBufferIO, Int] {
  def asReadOnly: IntBufferReadIO[F]
}


object LongBufferIO {
  trait Module[F[_]] extends BufferIO.Module[F, LongBufferIO]
}
object IsoLongBufferIO {
  trait Module[F[_]] extends LongBufferIO.Module[F] with IsoMutable[LongBufferIO, F, java.nio.LongBuffer]
}

trait LongBufferReadIO[F[_]] extends BufferReadIO[F, LongBufferReadIO, Long]
trait LongBufferIO[F[_]] extends BufferIO[F, LongBufferIO, Long] {
  def asReadOnly: LongBufferReadIO[F]
}


object FloatBufferIO {
  trait Module[F[_]] extends BufferIO.Module[F, FloatBufferIO]
}
object IsoFloatBufferIO {
  trait Module[F[_]] extends FloatBufferIO.Module[F] with IsoMutable[FloatBufferIO, F, java.nio.FloatBuffer]
}

trait FloatBufferReadIO[F[_]] extends BufferReadIO[F, FloatBufferReadIO, Float]
trait FloatBufferIO[F[_]] extends BufferIO[F, FloatBufferIO, Float] {
  def asReadOnly: FloatBufferReadIO[F]
}


object DoubleBufferIO {
  trait Module[F[_]] extends BufferIO.Module[F, DoubleBufferIO]
}
object IsoDoubleBufferIO {
  trait Module[F[_]] extends DoubleBufferIO.Module[F] with IsoMutable[DoubleBufferIO, F, java.nio.DoubleBuffer]
}

trait DoubleBufferReadIO[F[_]] extends BufferReadIO[F, DoubleBufferReadIO, Double]
trait DoubleBufferIO[F[_]] extends BufferIO[F, DoubleBufferIO, Double] {
  def asReadOnly: DoubleBufferReadIO[F]
}
