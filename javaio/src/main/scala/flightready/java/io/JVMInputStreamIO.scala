package flightready.java.io

import java.io.InputStream

import flightready.integration.effect.ThunkWrap


class JVMInputStreamIO[F[_]](is: InputStream, tw: ThunkWrap[F]) extends InputStreamIO[F] {
  private[java] def close: F[Unit] = tw(is.close)

  def available: F[Int] = tw(is.available)

  def reset: F[Unit] = tw{ reset; () }

  def mark(readLimit: Int): F[Unit] = tw(is.mark(readLimit))
  def markSupported: F[Boolean] = tw(is.markSupported)

  def read: F[Int] = tw(is.read)
  def readInto(bytesOut: Array[Byte]): F[Int] = tw(is.read(bytesOut))
  def readIntoSlice(bytesOut: Array[Byte], ofs: Int, len: Int): F[Int] = tw(is.read(bytesOut, ofs, len))

  def skip(n: Long): F[Long] = tw(is.skip(n))
}
