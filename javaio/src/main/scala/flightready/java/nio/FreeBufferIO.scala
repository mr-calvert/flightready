package flightready.java.nio

import java.nio.ByteOrder

import flightready.{Lift, Term}


trait BufferReadFs[IOA[X[_], _] <: BufferReadIO[X, B], B] {
  sealed trait BufferReadF[A[_[_], _]] extends Term[IOA, A]
  sealed trait BufferReadF1[A] extends BufferReadF[({ type AF[X[_], _] = A })#AF]

  case object Capacity extends BufferReadF1[Int] { def selectA[F[_], A](io: IOA[F, A]): F[Int] = io.capacity }
  case object IsDirect extends BufferReadF1[Boolean] { def selectA[F[_], A](io: IOA[F, A]): F[Boolean] = io.isDirect }
  case object Order extends BufferReadF1[ByteOrder] { def selectA[F[_], A](io: IOA[F, A]): F[ByteOrder] = io.order }
  case object Limit extends BufferReadF1[Int] { def selectA[F[_], A](io: IOA[F, A]): F[Int] = io.limit }
  case object Mark extends BufferReadF1[Unit] { def selectA[F[_], A](io: IOA[F, A]): F[Unit] = io.mark }
  case object Position extends BufferReadF1[Int] { def selectA[F[_], A](io: IOA[F, A]): F[Int] = io.position }
  case object HasRemaining extends BufferReadF1[Boolean] { def selectA[F[_], A](io: IOA[F, A]): F[Boolean] = io.hasRemaining }
  case object Remaining extends BufferReadF1[Int] { def selectA[F[_], A](io: IOA[F, A]): F[Int] = io.remaining }
  case object Clear extends BufferReadF1[Unit] { def selectA[F[_], A](io: IOA[F, A]): F[Unit] = io.clear }
  case object Flip extends BufferReadF1[Unit] { def selectA[F[_], A](io: IOA[F, A]): F[Unit] = io.flip }
  case object Reset extends BufferReadF1[Unit] { def selectA[F[_], A](io: IOA[F, A]): F[Unit] = io.reset }
  case object Rewind extends BufferReadF1[Unit] { def selectA[F[_], A](io: IOA[F, A]): F[Unit] = io.rewind }
  case object Get extends BufferReadF1[B] { def selectA[F[_], A](io: IOA[F, A]): F[B] = io.get }

  case class SetLimit(limit: Int) extends BufferReadF1[Unit] {
    def selectA[F[_], A](io: IOA[F, A]): F[Unit] = io.setLimit(limit)
  }
  case class SetPosition(position: Int) extends BufferReadF1[Unit] {
    def selectA[F[_], A](io: IOA[F, A]): F[Unit] = io.setPosition(position)
  }
  case class GetAt(idx: Int) extends BufferReadF1[B] {
    def selectA[F[_], A](io: IOA[F, A]): F[B] = io.getAt(idx)
  }
  case class GetInto(dst: Array[B]) extends BufferReadF1[Unit] {
    def selectA[F[_], A](io: IOA[F, A]): F[Unit] = io.getInto(dst)
  }
  case class GetIntoSlice(dst: Array[B], ofs: Int, len: Int) extends BufferReadF1[Unit] {
    def selectA[F[_], A](io: IOA[F, A]): F[Unit] = io.getIntoSlice(dst, ofs, len)
  }

  trait FreeBufferReadIO[F[_]] extends BufferReadIO[F, B] {
    def l: Lift[IOA, F]

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

trait BufferFs[IOA[X[_], _] <: BufferIO[X, B], B] extends BufferReadFs[IOA, B] {
  sealed trait BufferF[A[_[_], _]] extends BufferReadF[A]
  sealed trait BufferF1[A] extends BufferF[({ type AF[X[_], _] = A })#AF]

  case object HasArray extends BufferF1[Boolean] { def selectA[F[_], A](io: IOA[F, A]): F[Boolean] = io.hasArray }
  case object Array extends BufferF1[Array[B]] { def selectA[F[_], A](io: IOA[F, A]): F[Array[B]] = io.array }
  case object ArrayOffset extends BufferF1[Int] { def selectA[F[_], A](io: IOA[F, A]): F[Int] = io.arrayOffset }
  case object Compact extends BufferF1[Unit] { def selectA[F[_], A](io: IOA[F, A]): F[Unit] = io.compact }
  case class Put(b: B) extends BufferF1[Unit] { def selectA[F[_], A](io: IOA[F, A]): F[Unit] = io.put(b) }
  case class PutAt(idx: Int, b: B) extends BufferF1[Unit] { def selectA[F[_], A](io: IOA[F, A]): F[Unit] = io.putAt(idx, b) }
  case class PutArray(as: Array[B]) extends BufferF1[Unit] { def selectA[F[_], A](io: IOA[F, A]): F[Unit] = io.putArray(as) }
  case class PutArraySlice(as: Array[B], ofs: Int, len: Int) extends BufferF1[Unit] {
    def selectA[F[_], A](io: IOA[F, A]): F[Unit] = io.putArraySlice(as, ofs, len)
  }

  trait FreeBufferIO[F[_]] extends FreeBufferReadIO[F] with BufferIO[F, B] {
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


/*
trait ByteBufferReadFs[IO[X[_]] <: ByteBufferReadIO[X]] extends BufferReadFs[IO, Byte] {
  sealed trait ByteBufferReadF[A[_[_]]] extends BufferReadF[A]

  case object DuplicateRO extends ByteBufferReadF[ByteBufferReadIO] {
    def select[F[_]](io: IO[F]): F[ByteBufferReadIO[F]] = io.duplicateRO
  }
  case object SliceRO extends ByteBufferReadF[ByteBufferReadIO] {
    def select[F[_]](io: IO[F]): F[ByteBufferReadIO[F]] = io.sliceRO
  }
  case object AsCharBufferRO extends ByteBufferReadF[CharBufferReadIO] {
    def select[F[_]](io: IO[F]): F[CharBufferReadIO[F]] = io.asCharBufferRO
  }
  case object AsShortBufferRO extends ByteBufferReadF[ShortBufferReadIO] {
    def select[F[_]](io: IO[F]): F[ShortBufferReadIO[F]] = io.asShortBufferRO
  }
  case object AsIntBufferRO extends ByteBufferReadF[IntBufferReadIO] {
    def select[F[_]](io: IO[F]): F[IntBufferReadIO[F]] = io.asIntBufferRO
  }
  case object AsLongBufferRO extends ByteBufferReadF[LongBufferReadIO] {
    def select[F[_]](io: IO[F]): F[LongBufferReadIO[F]] = io.asLongBufferRO
  }
  case object AsFloatBufferRO extends ByteBufferReadF[FloatBufferReadIO] {
    def select[F[_]](io: IO[F]): F[FloatBufferReadIO[F]] = io.asFloatBufferRO
  }
  case object AsDoubleBufferRO extends ByteBufferReadF[DoubleBufferReadIO] {
    def select[F[_]](io: IO[F]): F[DoubleBufferReadIO[F]] = io.asDoubleBufferRO
  }

  trait FreeByteBufferReadIO[F[_]] extends FreeBufferReadIO[F] with ByteBufferReadIO[F] {
    def asCharBufferRO: F[CharBufferReadIO[F]] = l.fbound(AsCharBufferRO)
    def asDoubleBufferRO: F[DoubleBufferReadIO[F]] = l.fbound(AsDoubleBufferRO)
    def asFloatBufferRO: F[FloatBufferReadIO[F]] = l.fbound(AsFloatBufferRO)
    def asIntBufferRO: F[IntBufferReadIO[F]] = l.fbound(AsIntBufferRO)
    def asLongBufferRO: F[LongBufferReadIO[F]] = l.fbound(AsLongBufferRO)
    def asShortBufferRO: F[ShortBufferReadIO[F]] = l.fbound(AsShortBufferRO)
    def duplicateRO: F[ByteBufferReadIO[F]] = l.fbound(DuplicateRO)
    def sliceRO: F[ByteBufferReadIO[F]] = l.fbound(SliceRO)
  }
}


trait ByteBufferReadF[A[_[_]]] extends ByteBufferReadF.ByteBufferReadF[A]

object ByteBufferReadF extends ByteBufferReadFs[ByteBufferReadIO]

class FreeByteBufferReadIO[F[_]](implicit val l: Lift[ByteBufferReadIO, F])
  extends ByteBufferReadF.FreeByteBufferReadIO[F]


sealed trait ByteBufferF[A[_[_]]] extends ByteBufferF.BaseFs.BufferF[A] with ByteBufferF.BaseFs.ByteBufferReadF[A]

object ByteBufferF {
  object BaseFs extends BufferFs[ByteBufferIO, Byte] with ByteBufferReadFs[ByteBufferIO]

  sealed trait ByteBufferF1[A] extends ByteBufferF[({ type AF[_[_]] = A })#AF]

  case object DuplicateRW extends ByteBufferF[ByteBufferIO] {
    def select[F[_]](io: ByteBufferIO[F]): F[ByteBufferIO[F]] = io.duplicateRW
  }
  case object SliceRW extends ByteBufferF[ByteBufferIO] {
    def select[F[_]](io: ByteBufferIO[F]): F[ByteBufferIO[F]] = io.duplicateRW
  }
  case class SetByteOrder(bo: ByteOrder) extends ByteBufferF1[Unit] {
    def select[F[_]](io: ByteBufferIO[F]): F[Unit] = io.setByteOrder(bo)
  }
  case object AsCharBufferRW extends ByteBufferF[CharBufferIO] {
    def select[F[_]](io: ByteBufferIO[F]): F[CharBufferIO[F]] = io.asCharBufferRW
  }
  case object AsShortBufferRW extends ByteBufferF[ShortBufferIO] {
    def select[F[_]](io: ByteBufferIO[F]): F[ShortBufferIO[F]] = io.asShortBufferRW
  }
  case object AsIntBufferRW extends ByteBufferF[IntBufferIO] {
    def select[F[_]](io: ByteBufferIO[F]): F[IntBufferIO[F]] = io.asIntBufferRW
  }
  case object AsLongBufferRW extends ByteBufferF[LongBufferIO] {
    def select[F[_]](io: ByteBufferIO[F]): F[LongBufferIO[F]] = io.asLongBufferRW
  }
  case object AsFloatBufferRW extends ByteBufferF[FloatBufferIO] {
    def select[F[_]](io: ByteBufferIO[F]): F[FloatBufferIO[F]] = io.asFloatBufferRW
  }
  case object AsDoubleBufferRW extends ByteBufferF[DoubleBufferIO] {
    def select[F[_]](io: ByteBufferIO[F]): F[DoubleBufferIO[F]] = io.asDoubleBufferRW
  }
  case class PutBuffer[X[_]](buf: ByteBufferReadIO[X]) extends ByteBufferF1[Unit] {
    def select[F[_]](io: ByteBufferIO[F]): F[Unit] = ??? // needs issue #50 fixed before implementation
  }
  case class PutChar(c: Char) extends ByteBufferF1[Unit] {
    def select[F[_]](io: ByteBufferIO[F]): F[Unit] = io.putChar(c)
  }
  case class PutCharAt(idx: Int, c: Char) extends ByteBufferF1[Unit] {
    def select[F[_]](io: ByteBufferIO[F]): F[Unit] = io.putCharAt(idx, c)
  }
  case class PutShort(s: Short) extends ByteBufferF1[Unit] {
    def select[F[_]](io: ByteBufferIO[F]): F[Unit] = io.putShort(s)
  }
  case class PutShortAt(idx: Int, s: Short) extends ByteBufferF1[Unit] {
    def select[F[_]](io: ByteBufferIO[F]): F[Unit] = io.putShortAt(idx, s)
  }
  case class PutInt(i: Int) extends ByteBufferF1[Unit] {
    def select[F[_]](io: ByteBufferIO[F]): F[Unit] = io.putInt(i)
  }
  case class PutIntAt(idx: Int, i: Int) extends ByteBufferF1[Unit] {
    def select[F[_]](io: ByteBufferIO[F]): F[Unit] = io.putIntAt(idx, i)
  }
  case class PutLong(l: Long) extends ByteBufferF1[Unit] {
    def select[F[_]](io: ByteBufferIO[F]): F[Unit] = io.putLong(l)
  }
  case class PutLongAt(idx: Int, l: Long) extends ByteBufferF1[Unit] {
    def select[F[_]](io: ByteBufferIO[F]): F[Unit] = io.putLongAt(idx, l)
  }
  case class PutFloat(f: Float) extends ByteBufferF1[Unit] {
    def select[F[_]](io: ByteBufferIO[F]): F[Unit] = io.putFloat(f)
  }
  case class PutFloatAt(idx: Int, f: Float) extends ByteBufferF1[Unit] {
    def select[F[_]](io: ByteBufferIO[F]): F[Unit] = io.putFloatAt(idx, f)
  }
  case class PutDouble(d: Double) extends ByteBufferF1[Unit] {
    def select[F[_]](io: ByteBufferIO[F]): F[Unit] = io.putDouble(d)
  }
  case class PutDoubleAt(idx: Int, d: Double) extends ByteBufferF1[Unit] {
    def select[F[_]](io: ByteBufferIO[F]): F[Unit] = io.putDoubleAt(idx, d)
  }
}

class FreeByteBufferIO[F[_]](implicit val l: Lift[ByteBufferIO, F])
      extends ByteBufferF.BaseFs.FreeByteBufferReadIO[F] with ByteBufferF.BaseFs.FreeBufferIO[F] with ByteBufferIO[F] {

  import ByteBufferF._

  def duplicateRW: F[ByteBufferIO[F]] = l.fbound(DuplicateRW)
  def sliceRW: F[ByteBufferIO[F]] = l.fbound(SliceRW)
  def asCharBufferRW: F[CharBufferIO[F]] = l.fbound(AsCharBufferRW)
  def asDoubleBufferRW: F[DoubleBufferIO[F]] = l.fbound(AsDoubleBufferRW)
  def asFloatBufferRW: F[FloatBufferIO[F]] = l.fbound(AsFloatBufferRW)
  def asIntBufferRW: F[IntBufferIO[F]] = l.fbound(AsIntBufferRW)
  def asLongBufferRW: F[LongBufferIO[F]] = l.fbound(AsLongBufferRW)
  def asShortBufferRW: F[ShortBufferIO[F]] = l.fbound(AsShortBufferRW)
  def setByteOrder(bo: ByteOrder): F[Unit] = l(SetByteOrder(bo))
  def putBuffer(buf: ByteBufferReadIO[F])
               (implicit fm: flightready.integration.category.FlatMap[F]): F[Unit] = l(PutBuffer(buf))
  def putChar(c: Char): F[Unit] = l(PutChar(c))
  def putCharAt(idx: Int, c: Char): F[Unit] = l(PutCharAt(idx, c))
  def putDouble(d: Double): F[Unit] = l(PutDouble(d))
  def putDoubleAt(idx: Int, d: Double): F[Unit] = l(PutDoubleAt(idx, d))
  def putFloat(f: Float): F[Unit] = l(PutFloat(f))
  def putFloatAt(idx: Int, f: Float): F[Unit] = l(PutFloatAt(idx, f))
  def putInt(i: Int): F[Unit] = l(PutInt(i))
  def putIntAt(idx: Int, i: Int): F[Unit] = l(PutIntAt(idx, i))
  def putLong(long: Long): F[Unit] = l(PutLong(long))
  def putLongAt(idx: Int, long: Long): F[Unit] = l(PutLongAt(idx, long))
  def putShort(s: Short): F[Unit] = l(PutShort(s))
  def putShortAt(idx: Int, s: Short): F[Unit] = l(PutShortAt(idx, s))
}



trait CharBufferReadFs[IO[X[_]] <: CharBufferReadIO[X]] extends BufferReadFs[IO, Char] {
  sealed trait CharBufferReadF[A[_[_]]] extends BufferReadF[A]
  sealed trait CharBufferReadF1[A] extends CharBufferReadF[({ type AF[_[_]] = A })#AF]

  case object DuplicateRO extends CharBufferReadF[CharBufferReadIO] {
    def select[F[_]](io: IO[F]): F[CharBufferReadIO[F]] = io.duplicateRO
  }
  case object SliceRO extends CharBufferReadF[CharBufferReadIO] {
    def select[F[_]](io: IO[F]): F[CharBufferReadIO[F]] = io.sliceRO
  }
  case class CharAt(idx: Int) extends CharBufferReadF1[Char] {
    def select[F[_]](io: IO[F]): F[Char] = io.charAt(idx)
  }
  case class Read[X[_]](buf: CharBufferIO[X]) extends CharBufferReadF1[Int] {
    def select[F[_]](io: IO[F]): F[Int] = ??? // needs issue #50 fixed before implementation
  }
  case class SubSequenceRO(start: Int, end: Int) extends CharBufferReadF[CharBufferReadIO] {
    def select[F[_]](io: IO[F]): F[CharBufferReadIO[F]] = io.subSequenceRO(start, end)
  }

  trait FreeCharBufferReadIO[F[_]] extends FreeBufferReadIO[F] with CharBufferReadIO[F] {
    def charAt(idx: Int): F[Char] = l(CharAt(idx))
    def duplicateRO: F[CharBufferReadIO[F]] = l.fbound(DuplicateRO)
    def read(dst: CharBufferIO[F])(implicit fm: FlatMap[F]): F[Int] = l(Read(dst))
    def sliceRO: F[CharBufferReadIO[F]] = l.fbound(SliceRO)
    def subSequenceRO(start: Int, end: Int): F[CharBufferReadIO[F]] = l.fbound(SubSequenceRO(start, end))
  }

}


trait CharBufferReadF[A[_[_]]] extends CharBufferReadF.CharBufferReadF[A]

object CharBufferReadF extends CharBufferReadFs[CharBufferReadIO]

class FreeCharBufferReadIO[F[_]](implicit val l: Lift[CharBufferReadIO, F])
  extends CharBufferReadF.FreeCharBufferReadIO[F]


sealed trait CharBufferF[A[_[_]]] extends CharBufferF.BaseFs.BufferF[A]

object CharBufferF {
  object BaseFs extends BufferFs[CharBufferIO, Char] with CharBufferReadFs[CharBufferIO]

  sealed trait CharBufferF1[A] extends CharBufferF[({ type AF[_[_]] = A })#AF]

  case object DuplicateRW extends CharBufferF[CharBufferIO] {
    def select[F[_]](io: CharBufferIO[F]): F[CharBufferIO[F]] = io.duplicateRW
  }
  case object SliceRW extends CharBufferF[CharBufferIO] {
    def select[F[_]](io: CharBufferIO[F]): F[CharBufferIO[F]] = io.sliceRW
  }
  case class PutBuffer[X[_]](buf: CharBufferReadIO[X]) extends CharBufferF1[Unit] {
    def select[F[_]](io: CharBufferIO[F]): F[Unit] = ??? // implement after issue #50
  }
  case class SubSequenceRW(start: Int, end: Int) extends CharBufferF[CharBufferIO] {
    def select[F[_]](io: CharBufferIO[F]): F[CharBufferIO[F]] = io.subSequenceRW(start, end)
  }
  case class PutString(s: String) extends CharBufferF1[Unit] {
    def select[F[_]](io: CharBufferIO[F]): F[Unit] = io.putString(s)
  }
  case class PutStringSlice(s: String, start: Int, end: Int) extends CharBufferF1[Unit] {
    def select[F[_]](io: CharBufferIO[F]): F[Unit] = io.putStringSlice(s, start, end)
  }
}

class FreeCharBufferIO[F[_]](implicit val l: Lift[CharBufferIO, F])
      extends CharBufferF.BaseFs.FreeCharBufferReadIO[F] with CharBufferF.BaseFs.FreeBufferIO[F] with CharBufferIO[F] {

  import CharBufferF._

  def duplicateRW: F[CharBufferIO[F]] = l.fbound(DuplicateRW)
  def putBuffer(buf: CharBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit] = l(PutBuffer(buf))
  def putString(s: String): F[Unit] = l(PutString(s))
  def putStringSlice(s: String, start: Int, end: Int): F[Unit] = l(PutStringSlice(s, start, end))
  def sliceRW: F[CharBufferIO[F]] = l.fbound(SliceRW)
  def subSequenceRW(start: Int, end: Int): F[CharBufferIO[F]] = l.fbound(SubSequenceRW(start, end))
}



trait ShortBufferReadFs[IO[X[_]] <: ShortBufferReadIO[X]] extends BufferReadFs[IO, Short] {
  sealed trait ShortBufferReadF[A[_[_]]] extends BufferReadF[A]
  sealed trait ShortBufferReadF1[A] extends ShortBufferReadF[({ type AF[_[_]] = A })#AF]

  case object DuplicateRO extends ShortBufferReadF[ShortBufferReadIO] {
    def select[F[_]](io: IO[F]): F[ShortBufferReadIO[F]] = io.duplicateRO
  }
  case object SliceRO extends ShortBufferReadF[ShortBufferReadIO] {
    def select[F[_]](io: IO[F]): F[ShortBufferReadIO[F]] = io.sliceRO
  }

  trait FreeShortBufferReadIO[F[_]] extends FreeBufferReadIO[F] with ShortBufferReadIO[F] {
    def duplicateRO: F[ShortBufferReadIO[F]] = l.fbound(DuplicateRO)
    def sliceRO: F[ShortBufferReadIO[F]] = l.fbound(SliceRO)
  }
}

trait ShortBufferReadF[A[_[_]]] extends ShortBufferReadF.ShortBufferReadF[A]

object ShortBufferReadF extends ShortBufferReadFs[ShortBufferReadIO]

class FreeShortBufferReadIO[F[_]](implicit val l: Lift[ShortBufferReadIO, F])
  extends ShortBufferReadF.FreeShortBufferReadIO[F]


sealed trait ShortBufferF[A[_[_]]] extends ShortBufferF.BaseFs.BufferF[A]

object ShortBufferF {
  object BaseFs extends BufferFs[ShortBufferIO, Short] with ShortBufferReadFs[ShortBufferIO]

  sealed trait ShortBufferF1[A] extends ShortBufferF[({ type AF[_[_]] = A })#AF]

  case object DuplicateRW extends ShortBufferF[ShortBufferIO] {
    def select[F[_]](io: ShortBufferIO[F]): F[ShortBufferIO[F]] = io.duplicateRW
  }
  case object SliceRW extends ShortBufferF[ShortBufferIO] {
    def select[F[_]](io: ShortBufferIO[F]): F[ShortBufferIO[F]] = io.sliceRW
  }
  case class PutBuffer[X[_]](buf: ShortBufferReadIO[X]) extends ShortBufferF1[Unit] {
    def select[F[_]](io: ShortBufferIO[F]): F[Unit] = ??? // implement after issue #50
  }
}

class FreeShortBufferIO[F[_]](implicit val l: Lift[ShortBufferIO, F])
      extends ShortBufferF.BaseFs.FreeShortBufferReadIO[F]
        with ShortBufferF.BaseFs.FreeBufferIO[F]
        with ShortBufferIO[F] {

  import ShortBufferF._

  def duplicateRW: F[ShortBufferIO[F]] = l.fbound(DuplicateRW)
  def sliceRW: F[ShortBufferIO[F]] = l.fbound(SliceRW)

  def putBuffer(buf: ShortBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit] = l(PutBuffer(buf))
}



trait IntBufferReadFs[IO[X[_]] <: IntBufferReadIO[X]] extends BufferReadFs[IO, Int] {
  sealed trait IntBufferReadF[A[_[_]]] extends BufferReadF[A]
  sealed trait IntBufferReadF1[A] extends IntBufferReadF[({ type AF[_[_]] = A })#AF]

  case object DuplicateRO extends IntBufferReadF[IntBufferReadIO] {
    def select[F[_]](io: IO[F]): F[IntBufferReadIO[F]] = io.duplicateRO
  }
  case object SliceRO extends IntBufferReadF[IntBufferReadIO] {
    def select[F[_]](io: IO[F]): F[IntBufferReadIO[F]] = io.sliceRO
  }

  trait FreeIntBufferReadIO[F[_]] extends FreeBufferReadIO[F] with IntBufferReadIO[F] {
    def duplicateRO: F[IntBufferReadIO[F]] = l.fbound(DuplicateRO)
    def sliceRO: F[IntBufferReadIO[F]] = l.fbound(SliceRO)
  }
}

trait IntBufferReadF[A[_[_]]] extends IntBufferReadF.IntBufferReadF[A]

object IntBufferReadF extends IntBufferReadFs[IntBufferReadIO]

class FreeIntBufferReadIO[F[_]](implicit val l: Lift[IntBufferReadIO, F])
  extends IntBufferReadF.FreeIntBufferReadIO[F]


sealed trait IntBufferF[A[_[_]]] extends IntBufferF.BaseFs.BufferF[A]

object IntBufferF {
  object BaseFs extends BufferFs[IntBufferIO, Int] with IntBufferReadFs[IntBufferIO]

  sealed trait IntBufferF1[A] extends IntBufferF[({ type AF[_[_]] = A })#AF]

  case object DuplicateRW extends IntBufferF[IntBufferIO] {
    def select[F[_]](io: IntBufferIO[F]): F[IntBufferIO[F]] = io.duplicateRW
  }
  case object SliceRW extends IntBufferF[IntBufferIO] {
    def select[F[_]](io: IntBufferIO[F]): F[IntBufferIO[F]] = io.sliceRW
  }
  case class PutBuffer[X[_]](buf: IntBufferReadIO[X]) extends IntBufferF1[Unit] {
    def select[F[_]](io: IntBufferIO[F]): F[Unit] = ??? // implement after issue #50
  }
}

class FreeIntBufferIO[F[_]](implicit val l: Lift[IntBufferIO, F])
      extends IntBufferF.BaseFs.FreeIntBufferReadIO[F] with IntBufferF.BaseFs.FreeBufferIO[F] with IntBufferIO[F] {

  import IntBufferF._

  def duplicateRW: F[IntBufferIO[F]] = l.fbound(DuplicateRW)
  def sliceRW: F[IntBufferIO[F]] = l.fbound(SliceRW)

  def putBuffer(buf: IntBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit] = l(PutBuffer(buf))
}



trait LongBufferReadFs[IO[X[_]] <: LongBufferReadIO[X]] extends BufferReadFs[IO, Long] {
  sealed trait LongBufferReadF[A[_[_]]] extends BufferReadF[A]
  sealed trait LongBufferReadF1[A] extends LongBufferReadF[({ type AF[_[_]] = A })#AF]

  case object DuplicateRO extends LongBufferReadF[LongBufferReadIO] {
    def select[F[_]](io: IO[F]): F[LongBufferReadIO[F]] = io.duplicateRO
  }
  case object SliceRO extends LongBufferReadF[LongBufferReadIO] {
    def select[F[_]](io: IO[F]): F[LongBufferReadIO[F]] = io.sliceRO
  }

  trait FreeLongBufferReadIO[F[_]] extends FreeBufferReadIO[F] with LongBufferReadIO[F] {
    def duplicateRO: F[LongBufferReadIO[F]] = l.fbound(DuplicateRO)
    def sliceRO: F[LongBufferReadIO[F]] = l.fbound(SliceRO)
  }
}

trait LongBufferReadF[A[_[_]]] extends LongBufferReadF.LongBufferReadF[A]

object LongBufferReadF extends LongBufferReadFs[LongBufferReadIO]

class FreeLongBufferReadIO[F[_]](implicit val l: Lift[LongBufferReadIO, F])
  extends LongBufferReadF.FreeLongBufferReadIO[F]


sealed trait LongBufferF[A[_[_]]] extends LongBufferF.BaseFs.BufferF[A]

object LongBufferF {
  object BaseFs extends BufferFs[LongBufferIO, Long] with LongBufferReadFs[LongBufferIO]

  sealed trait LongBufferF1[A] extends LongBufferF[({ type AF[_[_]] = A })#AF]

  case object DuplicateRW extends LongBufferF[LongBufferIO] {
    def select[F[_]](io: LongBufferIO[F]): F[LongBufferIO[F]] = io.duplicateRW
  }
  case object SliceRW extends LongBufferF[LongBufferIO] {
    def select[F[_]](io: LongBufferIO[F]): F[LongBufferIO[F]] = io.sliceRW
  }
  case class PutBuffer[X[_]](buf: LongBufferReadIO[X]) extends LongBufferF1[Unit] {
    def select[F[_]](io: LongBufferIO[F]): F[Unit] = ??? // implement after issue #50
  }
}

class FreeLongBufferIO[F[_]](implicit val l: Lift[LongBufferIO, F])
      extends LongBufferF.BaseFs.FreeLongBufferReadIO[F] with LongBufferF.BaseFs.FreeBufferIO[F] with LongBufferIO[F] {

  import LongBufferF._

  def duplicateRW: F[LongBufferIO[F]] = l.fbound(DuplicateRW)
  def sliceRW: F[LongBufferIO[F]] = l.fbound(SliceRW)

  def putBuffer(buf: LongBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit] = l(PutBuffer(buf))
}



trait FloatBufferReadFs[IO[X[_]] <: FloatBufferReadIO[X]] extends BufferReadFs[IO, Float] {
  sealed trait FloatBufferReadF[A[_[_]]] extends BufferReadF[A]
  sealed trait FloatBufferReadF1[A] extends FloatBufferReadF[({ type AF[_[_]] = A })#AF]

  case object DuplicateRO extends FloatBufferReadF[FloatBufferReadIO] {
    def select[F[_]](io: IO[F]): F[FloatBufferReadIO[F]] = io.duplicateRO
  }
  case object SliceRO extends FloatBufferReadF[FloatBufferReadIO] {
    def select[F[_]](io: IO[F]): F[FloatBufferReadIO[F]] = io.sliceRO
  }

  trait FreeFloatBufferReadIO[F[_]] extends FreeBufferReadIO[F] with FloatBufferReadIO[F] {
    def duplicateRO: F[FloatBufferReadIO[F]] = l.fbound(DuplicateRO)
    def sliceRO: F[FloatBufferReadIO[F]] = l.fbound(SliceRO)
  }
}


trait FloatBufferReadF[A[_[_]]] extends FloatBufferReadF.FloatBufferReadF[A]

object FloatBufferReadF extends FloatBufferReadFs[FloatBufferReadIO]

class FreeFloatBufferReadIO[F[_]](implicit val l: Lift[FloatBufferReadIO, F])
  extends FloatBufferReadF.FreeFloatBufferReadIO[F]


sealed trait FloatBufferF[A[_[_]]] extends FloatBufferF.BaseFs.BufferF[A]

object FloatBufferF {
  object BaseFs extends BufferFs[FloatBufferIO, Float] with FloatBufferReadFs[FloatBufferIO]

  sealed trait FloatBufferF1[A] extends FloatBufferF[({ type AF[_[_]] = A })#AF]

  case object DuplicateRW extends FloatBufferF[FloatBufferIO] {
    def select[F[_]](io: FloatBufferIO[F]): F[FloatBufferIO[F]] = io.duplicateRW
  }
  case object SliceRW extends FloatBufferF[FloatBufferIO] {
    def select[F[_]](io: FloatBufferIO[F]): F[FloatBufferIO[F]] = io.sliceRW
  }
  case class PutBuffer[X[_]](buf: FloatBufferReadIO[X]) extends FloatBufferF1[Unit] {
    def select[F[_]](io: FloatBufferIO[F]): F[Unit] = ??? // implement after issue #50
  }
}

class FreeFloatBufferIO[F[_]](implicit val l: Lift[FloatBufferIO, F])
      extends FloatBufferF.BaseFs.FreeFloatBufferReadIO[F] with FloatBufferF.BaseFs.FreeBufferIO[F] with FloatBufferIO[F] {

  import FloatBufferF._

  def duplicateRW: F[FloatBufferIO[F]] = l.fbound(DuplicateRW)
  def sliceRW: F[FloatBufferIO[F]] = l.fbound(SliceRW)

  def putBuffer(buf: FloatBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit] = l(PutBuffer(buf))
}



trait DoubleBufferReadFs[IO[X[_]] <: DoubleBufferReadIO[X]] extends BufferReadFs[IO, Double] {
  sealed trait DoubleBufferReadF[A[_[_]]] extends BufferReadF[A]
  sealed trait DoubleBufferReadF1[A] extends DoubleBufferReadF[({ type AF[_[_]] = A })#AF]

  case object DuplicateRO extends DoubleBufferReadF[DoubleBufferReadIO] {
    def select[F[_]](io: IO[F]): F[DoubleBufferReadIO[F]] = io.duplicateRO
  }
  case object SliceRO extends DoubleBufferReadF[DoubleBufferReadIO] {
    def select[F[_]](io: IO[F]): F[DoubleBufferReadIO[F]] = io.sliceRO
  }

  trait FreeDoubleBufferReadIO[F[_]] extends FreeBufferReadIO[F] with DoubleBufferReadIO[F] {
    def duplicateRO: F[DoubleBufferReadIO[F]] = l.fbound(DuplicateRO)
    def sliceRO: F[DoubleBufferReadIO[F]] = l.fbound(SliceRO)
  }
}


trait DoubleBufferReadF[A[_[_]]] extends DoubleBufferReadF.DoubleBufferReadF[A]

object DoubleBufferReadF extends DoubleBufferReadFs[DoubleBufferReadIO]

class FreeDoubleBufferReadIO[F[_]](implicit val l: Lift[DoubleBufferReadIO, F])
  extends DoubleBufferReadF.FreeDoubleBufferReadIO[F]


sealed trait DoubleBufferF[A[_[_]]] extends DoubleBufferF.BaseFs.BufferF[A]

object DoubleBufferF {
  object BaseFs extends BufferFs[DoubleBufferIO, Double] with DoubleBufferReadFs[DoubleBufferIO]

  sealed trait DoubleBufferF1[A] extends DoubleBufferF[({ type AF[_[_]] = A })#AF]

  case object DuplicateRW extends DoubleBufferF[DoubleBufferIO] {
    def select[F[_]](io: DoubleBufferIO[F]): F[DoubleBufferIO[F]] = io.duplicateRW
  }
  case object SliceRW extends DoubleBufferF[DoubleBufferIO] {
    def select[F[_]](io: DoubleBufferIO[F]): F[DoubleBufferIO[F]] = io.sliceRW
  }
  case class PutBuffer[X[_]](buf: DoubleBufferReadIO[X]) extends DoubleBufferF1[Unit] {
    def select[F[_]](io: DoubleBufferIO[F]): F[Unit] = ??? // implement after issue #50
  }
}

class FreeDoubleBufferIO[F[_]](implicit val l: Lift[DoubleBufferIO, F])
      extends DoubleBufferF.BaseFs.FreeDoubleBufferReadIO[F] with DoubleBufferF.BaseFs.FreeBufferIO[F] with DoubleBufferIO[F] {

  import DoubleBufferF._

  def duplicateRW: F[DoubleBufferIO[F]] = l.fbound(DuplicateRW)
  def sliceRW: F[DoubleBufferIO[F]] = l.fbound(SliceRW)

  def putBuffer(buf: DoubleBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit] = l(PutBuffer(buf))
}
 */
