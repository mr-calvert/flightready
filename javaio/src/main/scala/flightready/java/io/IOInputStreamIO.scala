package flightready.java.io

import java.io.InputStream

import flightready.integration.effect.ThunkWrap

import scala.language.higherKinds

class IOInputStreamIO[F[_]](is: InputStream, tw: ThunkWrap[F]) extends InputStreamIO[F] {
  private[java] def close: F[Unit] = tw.wrap(is.close)

  def available: F[Int] = tw.wrap(is.available)

  def reset: F[Unit] = tw.wrap(reset)

  def mark(readLimit: Int): F[Unit] = tw.wrap(is.mark(readLimit))
  def markSupported: F[Boolean] = tw.wrap(is.markSupported)

  def read: F[Int] = tw.wrap(is.read)
  def readInto(bytesOut: Array[Byte]): F[Int] = tw.wrap(is.read(bytesOut))
  def readIntoSlice(bytesOut: Array[Byte], ofs: Int, len: Int): F[Int] = tw.wrap(is.read(bytesOut, ofs, len))

  def skip(n: Long): F[Long] = tw.wrap(is.skip(n))
}
