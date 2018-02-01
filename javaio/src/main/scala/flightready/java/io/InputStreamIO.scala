package flightready.java.io


/** Operations upon an opn [[java.io.InputStream]]
  *
  * The close operation has been elided for type safety.
  */
trait InputStreamIO[F[_], A] {
  def available: F[Int]

  def reset: F[Unit]

  def mark(readLimit: Int): F[Unit]
  def markSupported: F[Boolean]

  def read: F[Int]
  def readInto(bytesOut: Array[Byte]): F[Int]
  def readIntoSlice(bytesOut: Array[Byte], ofs: Int, len: Int): F[Int]

  def skip(n: Long): F[Long]

  private[java] def close: F[Unit]
}
