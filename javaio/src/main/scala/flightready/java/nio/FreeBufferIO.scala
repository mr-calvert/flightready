package flightready.java.nio

import java.nio.ByteOrder

import flightready.{Term, Lift}


class BufferReadFs[IO[X[_]] <: BufferReadIO[X, B], B] {
  sealed trait BufferReadF[A] extends Term[IO, A]

  case object Capacity extends BufferReadF[Int] { def select[F[_]](io: IO[F]): F[Int] = io.capacity }
  case object IsDirect extends BufferReadF[Boolean] { def select[F[_]](io: IO[F]): F[Boolean] = io.isDirect }
  case object Order extends BufferReadF[ByteOrder] { def select[F[_]](io: IO[F]): F[ByteOrder] = io.order }
  case object Limit extends BufferReadF[Int] { def select[F[_]](io: IO[F]): F[Int] = io.limit }
  case object Mark extends BufferReadF[Unit] { def select[F[_]](io: IO[F]): F[Unit] = io.mark }
  case object Position extends BufferReadF[Int] { def select[F[_]](io: IO[F]): F[Int] = io.position }
  case object HasRemaining extends BufferReadF[Boolean] { def select[F[_]](io: IO[F]): F[Boolean] = io.hasRemaining }
  case object Remaining extends BufferReadF[Int] { def select[F[_]](io: IO[F]): F[Int] = io.remaining }
  case object Clear extends BufferReadF[Unit] { def select[F[_]](io: IO[F]): F[Unit] = io.clear }
  case object Flip extends BufferReadF[Unit] { def select[F[_]](io: IO[F]): F[Unit] = io.flip }
  case object Reset extends BufferReadF[Unit] { def select[F[_]](io: IO[F]): F[Unit] = io.reset }
  case object Rewind extends BufferReadF[Unit] { def select[F[_]](io: IO[F]): F[Unit] = io.rewind }
  case object Get extends BufferReadF[B] { def select[F[_]](io: IO[F]): F[B] = io.get }

  case class SetLimit(limit: Int) extends BufferReadF[Unit] {
    def select[F[_]](io: IO[F]): F[Unit] = io.setLimit(limit)
  }
  case class SetPosition(position: Int) extends BufferReadF[Unit] {
    def select[F[_]](io: IO[F]): F[Unit] = io.setPosition(position)
  }
  case class GetAt(idx: Int) extends BufferReadF[B] {
    def select[F[_]](io: IO[F]): F[B] = io.getAt(idx)
  }
  case class GetInto(dst: Array[B]) extends BufferReadF[Unit] {
    def select[F[_]](io: IO[F]): F[Unit] = io.getInto(dst)
  }
  case class GetIntoSlice(dst: Array[B], ofs: Int, len: Int) extends BufferReadF[Unit] {
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
  sealed trait BufferF[A] extends BufferReadF[A]

  case object HasArray extends BufferF[Boolean] { def select[F[_]](io: IO[F]): F[Boolean] = io.hasArray }
  case object Array extends BufferF[Array[B]] { def select[F[_]](io: IO[F]): F[Array[B]] = io.array }
  case object ArrayOffset extends BufferF[Int] { def select[F[_]](io: IO[F]): F[Int] = io.arrayOffset }
  case object Compact extends BufferF[Unit] { def select[F[_]](io: IO[F]): F[Unit] = io.compact }
  case class Put(b: B) extends BufferF[Unit] { def select[F[_]](io: IO[F]): F[Unit] = io.put(b) }
  case class PutAt(idx: Int, b: B) extends BufferF[Unit] { def select[F[_]](io: IO[F]): F[Unit] = io.putAt(idx, b) }
  case class PutArray(as: Array[B]) extends BufferF[Unit] { def select[F[_]](io: IO[F]): F[Unit] = io.putArray(as) }
  case class PutArraySlice(as: Array[B], ofs: Int, len: Int) extends BufferF[Unit] {
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

sealed trait ByteBufferReadF[A] extends ByteBufferReadF.BaseFs.BufferReadF[A]

object ByteBufferReadF {
  object BaseFs extends BufferReadFs[ByteBufferReadIO, Byte]

//  case object DuplicateRO extends ByteBufferReadF[ByteBufferReadIO[_]] {
//    def select[F[_]](io: ByteBufferReadIO[F]): F[ByteBufferReadIO[F]] = io.duplicateRO
//  }

}

//class FreeByteBufferReadIO[F[_]](implicit l: Lift[ByteBufferIO, F]) extends ByteBufferIO[F] {
//
//}

object FreeBufferIO
