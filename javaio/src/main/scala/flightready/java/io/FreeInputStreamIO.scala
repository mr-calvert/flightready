package flightready.java.io

import flightready.{Lift1, Term1}


sealed trait InputStreamF[A] extends Term1[InputStreamIO, A]

object InputStreamF {
  case object Available extends InputStreamF[Int] { def select[F[_]](io: InputStreamIO[F]): F[Int] = io.available }
  case object Reset extends InputStreamF[Unit] { def select[F[_]](io: InputStreamIO[F]): F[Unit] = io.reset }
  case object Read extends InputStreamF[Int] { def select[F[_]](io: InputStreamIO[F]): F[Int] = io.read }

  final case class Mark(readLimit: Int) extends InputStreamF[Unit] {
    def select[F[_]](io: InputStreamIO[F]): F[Unit] = io.mark(readLimit)
  }
  case object MarkSupported extends InputStreamF[Boolean] {
    def select[F[_]](io: InputStreamIO[F]): F[Boolean] = io.markSupported
  }
  final case class ReadInto(bytesOut: Array[Byte]) extends InputStreamF[Int] {
    def select[F[_]](io: InputStreamIO[F]): F[Int] = io.readInto(bytesOut)
  }
  final case class ReadIntoSlice(bytesOut: Array[Byte], ofs: Int, len: Int) extends InputStreamF[Int] {
    def select[F[_]](io: InputStreamIO[F]): F[Int] = io.readIntoSlice(bytesOut, ofs, len)
  }
  final case class Skip(n: Long) extends InputStreamF[Long] {
    def select[F[_]](io: InputStreamIO[F]): F[Long] = io.skip(n)
  }
}

class FreeInputStreamIO[F[_]](implicit l: Lift1[InputStreamIO, F]) extends InputStreamIO[F] {
  import InputStreamF._

  def available: F[Int] = l(Available)
  def reset: F[Unit] = l(Reset)
  def mark(readLimit: Int): F[Unit] = l(Mark(readLimit))
  def markSupported: F[Boolean] = l(MarkSupported)
  def read: F[Int] = l(Read)
  def readInto(bytesOut: Array[Byte]): F[Int] = l(ReadInto(bytesOut))
  def readIntoSlice(bytesOut: Array[Byte], ofs: Int, len: Int): F[Int] = l(ReadIntoSlice(bytesOut, ofs, len))
  def skip(n: Long): F[Long] = l(Skip(n))
}
