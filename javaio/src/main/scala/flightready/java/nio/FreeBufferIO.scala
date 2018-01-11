package flightready.java.nio

import java.nio.ByteOrder

import flightready.java.nio.ByteBufferReadF._
import flightready.{Lift, Term}


class BufferReadFs[IO[X[_]] <: BufferReadIO[X, B], B] {
  sealed trait BufferReadF[A[_[_]]] extends Term[IO, A]
  sealed trait BufferReadF1[A] extends BufferReadF[({ type AF[_[_]] = A })#AF]

  case object Capacity extends BufferReadF1[Int] { def select[F[_]](io: IO[F]): F[Int] = io.capacity }
  case object IsDirect extends BufferReadF1[Boolean] { def select[F[_]](io: IO[F]): F[Boolean] = io.isDirect }
  case object Order extends BufferReadF1[ByteOrder] { def select[F[_]](io: IO[F]): F[ByteOrder] = io.order }
  case object Limit extends BufferReadF1[Int] { def select[F[_]](io: IO[F]): F[Int] = io.limit }
  case object Mark extends BufferReadF1[Unit] { def select[F[_]](io: IO[F]): F[Unit] = io.mark }
  case object Position extends BufferReadF1[Int] { def select[F[_]](io: IO[F]): F[Int] = io.position }
  case object HasRemaining extends BufferReadF1[Boolean] { def select[F[_]](io: IO[F]): F[Boolean] = io.hasRemaining }
  case object Remaining extends BufferReadF1[Int] { def select[F[_]](io: IO[F]): F[Int] = io.remaining }
  case object Clear extends BufferReadF1[Unit] { def select[F[_]](io: IO[F]): F[Unit] = io.clear }
  case object Flip extends BufferReadF1[Unit] { def select[F[_]](io: IO[F]): F[Unit] = io.flip }
  case object Reset extends BufferReadF1[Unit] { def select[F[_]](io: IO[F]): F[Unit] = io.reset }
  case object Rewind extends BufferReadF1[Unit] { def select[F[_]](io: IO[F]): F[Unit] = io.rewind }
  case object Get extends BufferReadF1[B] { def select[F[_]](io: IO[F]): F[B] = io.get }

  case class SetLimit(limit: Int) extends BufferReadF1[Unit] {
    def select[F[_]](io: IO[F]): F[Unit] = io.setLimit(limit)
  }
  case class SetPosition(position: Int) extends BufferReadF1[Unit] {
    def select[F[_]](io: IO[F]): F[Unit] = io.setPosition(position)
  }
  case class GetAt(idx: Int) extends BufferReadF1[B] {
    def select[F[_]](io: IO[F]): F[B] = io.getAt(idx)
  }
  case class GetInto(dst: Array[B]) extends BufferReadF1[Unit] {
    def select[F[_]](io: IO[F]): F[Unit] = io.getInto(dst)
  }
  case class GetIntoSlice(dst: Array[B], ofs: Int, len: Int) extends BufferReadF1[Unit] {
    def select[F[_]](io: IO[F]): F[Unit] = io.getIntoSlice(dst, ofs, len)
  }

  class FreeBufferReadIO[F[_]](implicit l: Lift[IO, F]) extends BufferReadIO[F, B] {
    def capacity: F[Int] = l(Capacity)

    def isDirect: F[Boolean] = l(IsDirect)
    def order: F[ByteOrder] = l(Order)

    def limit: F[Int] = l(Limit)
    def mark: F[Unit] = l(Mark)
    def position: F[Int] = l(Position)
    def hasRemaining: F[Boolean] = l(HasRemaining)
    def remaining: F[Int] = l(Remaining)

    def setLimit(limit: Int): F[Unit] = l(SetLimit(limit))
    def setPosition(position: Int): F[Unit] = l(SetPosition(position))

    def clear: F[Unit] = l(Clear)
    def flip: F[Unit] = l(Flip)
    def reset: F[Unit] = l(Reset)
    def rewind: F[Unit] = l(Rewind)

    def get: F[B] = l(Get)
    def getAt(idx: Int): F[B] = l(GetAt(idx))
    def getInto(dst: Array[B]): F[Unit] = l(GetInto(dst))
    def getIntoSlice(dst: Array[B], ofs: Int, len: Int): F[Unit] = l(GetIntoSlice(dst, ofs, len))
  }
}

class BufferIOFs[IO[X[_]] <: BufferIO[X, B], B] extends BufferReadFs[IO, B] {
  sealed trait BufferF[A[_[_]]] extends BufferReadF[A]
  sealed trait BufferF1[A] extends BufferF[({ type AF[_[_]] = A })#AF]

  case object HasArray extends BufferF1[Boolean] { def select[F[_]](io: IO[F]): F[Boolean] = io.hasArray }
  case object Array extends BufferF1[Array[B]] { def select[F[_]](io: IO[F]): F[Array[B]] = io.array }
  case object ArrayOffset extends BufferF1[Int] { def select[F[_]](io: IO[F]): F[Int] = io.arrayOffset }
  case object Compact extends BufferF1[Unit] { def select[F[_]](io: IO[F]): F[Unit] = io.compact }
  case class Put(b: B) extends BufferF1[Unit] { def select[F[_]](io: IO[F]): F[Unit] = io.put(b) }
  case class PutAt(idx: Int, b: B) extends BufferF1[Unit] { def select[F[_]](io: IO[F]): F[Unit] = io.putAt(idx, b) }
  case class PutArray(as: Array[B]) extends BufferF1[Unit] { def select[F[_]](io: IO[F]): F[Unit] = io.putArray(as) }
  case class PutArraySlice(as: Array[B], ofs: Int, len: Int) extends BufferF1[Unit] {
    def select[F[_]](io: IO[F]): F[Unit] = io.putArraySlice(as, ofs, len)
  }

  class FreeBufferIO[F[_]](implicit l: Lift[IO, F]) extends FreeBufferReadIO[F] with BufferIO[F, B] {
    def hasArray: F[Boolean] = l(HasArray)
    def array: F[Array[B]] = l(Array)
    def arrayOffset: F[Int] = l(ArrayOffset)
    def compact: F[Unit] = l(Compact)
    def put(b: B): F[Unit] = l(Put(b))
    def putAt(idx: Int, b: B): F[Unit] = l(PutAt(idx, b))
    def putArray(as: Array[B]): F[Unit] = l(PutArray(as))
    def putArraySlice(as: Array[B], ofs: Int, len: Int): F[Unit] = l(PutArraySlice(as, ofs, len))
  }
}

sealed trait ByteBufferReadF[A[_[_]]] extends ByteBufferReadF.BaseFs.BufferReadF[A]

object ByteBufferReadF {
  object BaseFs extends BufferReadFs[ByteBufferReadIO, Byte]

  case object DuplicateRO extends ByteBufferReadF[ByteBufferReadIO] {
    def select[F[_]](io: ByteBufferReadIO[F]): F[ByteBufferReadIO[F]] = io.duplicateRO
  }
  case object SliceRO extends ByteBufferReadF[ByteBufferReadIO] {
    def select[F[_]](io: ByteBufferReadIO[F]): F[ByteBufferReadIO[F]] = io.sliceRO
  }
  case object AsCharBufferRO extends ByteBufferReadF[CharBufferReadIO] {
    def select[F[_]](io: ByteBufferReadIO[F]): F[CharBufferReadIO[F]] = io.asCharBufferRO
  }
  case object AsShortBufferRO extends ByteBufferReadF[ShortBufferReadIO] {
    def select[F[_]](io: ByteBufferReadIO[F]): F[ShortBufferReadIO[F]] = io.asShortBufferRO
  }
  case object AsIntBufferRO extends ByteBufferReadF[IntBufferReadIO] {
    def select[F[_]](io: ByteBufferReadIO[F]): F[IntBufferReadIO[F]] = io.asIntBufferRO
  }
  case object AsLongBufferRO extends ByteBufferReadF[LongBufferReadIO] {
    def select[F[_]](io: ByteBufferReadIO[F]): F[LongBufferReadIO[F]] = io.asLongBufferRO
  }
  case object AsFloatBufferRO extends ByteBufferReadF[FloatBufferReadIO] {
    def select[F[_]](io: ByteBufferReadIO[F]): F[FloatBufferReadIO[F]] = io.asFloatBufferRO
  }
  case object AsDoubleBufferRO extends ByteBufferReadF[DoubleBufferReadIO] {
    def select[F[_]](io: ByteBufferReadIO[F]): F[DoubleBufferReadIO[F]] = io.asDoubleBufferRO
  }
}

class FreeByteBufferReadIO[F[_]](implicit l: Lift[ByteBufferReadIO, F])
      extends ByteBufferReadF.BaseFs.FreeBufferReadIO with ByteBufferReadIO[F] {

  def asCharBufferRO: F[flightready.java.nio.CharBufferReadIO[F]] = l.fbound(AsCharBufferRO)
  def asDoubleBufferRO: F[flightready.java.nio.DoubleBufferReadIO[F]] = l.fbound(AsDoubleBufferRO)
  def asFloatBufferRO: F[flightready.java.nio.FloatBufferReadIO[F]] = l.fbound(AsFloatBufferRO)
  def asIntBufferRO: F[flightready.java.nio.IntBufferReadIO[F]] = l.fbound(AsIntBufferRO)
  def asLongBufferRO: F[flightready.java.nio.LongBufferReadIO[F]] = l.fbound(AsLongBufferRO)
  def asShortBufferRO: F[flightready.java.nio.ShortBufferReadIO[F]] = l.fbound(AsShortBufferRO)
  def duplicateRO: F[flightready.java.nio.ByteBufferReadIO[F]] = l.fbound(DuplicateRO)
  def sliceRO: F[flightready.java.nio.ByteBufferReadIO[F]] = l.fbound(SliceRO)
}
