package flightready.java.nio


trait ByteChannelReadIO[F[_], A] {
  def read(bbioOut: ByteBufferIO[F, A]): F[Int]

  private[java] def close: F[Unit]
}

trait ByteChannelWriteIO[F[_], A] {
  def write(bbioIn: ByteBufferReadIO[F, A]): F[Int]

  private[java] def close: F[Unit]
}

trait SeekableByteChannelReadIO[F[_], A] extends ByteChannelReadIO[F, A] {
  def position: F[Long]
  def setPosition(pos: Long): F[Unit]

  def size: F[Long]

  def truncate(size: Long): F[Unit]

  private[java] def close: F[Unit]
}

trait SeekableByteChannelIO[F[_], A] extends SeekableByteChannelReadIO[F, A] with ByteChannelWriteIO[F, A]
