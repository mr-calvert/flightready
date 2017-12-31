package flightready.java.nio

import java.nio.channels.{SeekableByteChannel, ReadableByteChannel, WritableByteChannel}

import flightready.IsoMutable
import flightready.integration.effect.ThunkWrap


object JVMByteChannelReadIO {
  def isoMutable[F[_]](implicit tw: ThunkWrap[F]): IsoMutable[JVMByteChannelReadIO[F], ReadableByteChannel] =
    new IsoMutable[JVMByteChannelReadIO[F], ReadableByteChannel] {
      def toMutable(io: JVMByteChannelReadIO[F]): ReadableByteChannel = io.rbc
      def toIO(rbc: ReadableByteChannel): JVMByteChannelReadIO[F] =
        new JVMByteChannelReadIO[F](rbc, tw)
    }
}

class JVMByteChannelReadIO[F[_]](private[nio] val rbc: ReadableByteChannel, tw: ThunkWrap[F])
      extends ByteChannelReadIO[F, JVMByteBufferModule[F]] {

  def read(bbioOut: JVMByteBufferModule[F]#IORW): F[Int] =
    tw(rbc.read(JVMByteBufferModule[F](tw).isoMutableRORW.toMutable(bbioOut)))
}


object JVMByteChannelWriteIO {
  def isoMutable[F[_]](implicit tw: ThunkWrap[F]): IsoMutable[JVMByteChannelWriteIO[F], WritableByteChannel] =
    new IsoMutable[JVMByteChannelWriteIO[F], WritableByteChannel] {
      def toMutable(io: JVMByteChannelWriteIO[F]): WritableByteChannel = io.wbc
      def toIO(wbc: WritableByteChannel): JVMByteChannelWriteIO[F] =
        new JVMByteChannelWriteIO[F](wbc, tw)
    }
}

class JVMByteChannelWriteIO[F[_]](private[nio] val wbc: WritableByteChannel, tw: ThunkWrap[F])
      extends ByteChannelWriteIO[F, JVMByteBufferModule[F]] {

  private[java] def close: F[Unit] = tw(wbc.close)

  def write(bbioIn: JVMByteBufferModule[F]#IORO): F[Int] =
    tw(wbc.write(JVMByteBufferModule[F](tw).isoMutableRORW.toMutable(bbioIn)))
}


object JVMSeekableByteChannelIO {
  // NOTE: no conversion to NIOSeekableByteChannelIO is provided as
  // there is no way to check a SeekableByteChannel to see if it has
  // write permissions. Want a read/write instance? Use one of the
  // safe open methods in FSIO or force the issue by calling
  // `new NIOSeekableByteChannelReadIO` yourself!
  def isoMutableRORW[F[_]](implicit tw: ThunkWrap[F]):
        IsoMutable[JVMSeekableByteChannelReadIO[F], SeekableByteChannel] =

    new IsoMutable[JVMSeekableByteChannelReadIO[F], SeekableByteChannel] {
      def toMutable(io: JVMSeekableByteChannelReadIO[F]): SeekableByteChannel = io.sbc
      def toIO(sbc: SeekableByteChannel): JVMSeekableByteChannelReadIO[F] =
        new JVMSeekableByteChannelReadIO[F](sbc, tw)
    }
}

class JVMSeekableByteChannelReadIO[F[_]](private[nio] val sbc: SeekableByteChannel, tw: ThunkWrap[F])
      extends JVMByteChannelReadIO[F](sbc, tw) with SeekableByteChannelReadIO[F, JVMByteBufferModule[F]] {

  private[java] def close: F[Unit] = tw(sbc.close)

  def position: F[Long] = tw(sbc.position)
  def setPosition(pos: Long): F[Unit] = tw{ sbc.position(pos); () }
  def size: F[Long] = tw(sbc.size)
  def truncate(size: Long): F[Unit] = tw { sbc.truncate(size); () }
}

class JVMSeekableByteChannelIO[F[_]](sbc: SeekableByteChannel, tw: ThunkWrap[F])
    extends JVMSeekableByteChannelReadIO[F](sbc, tw) with SeekableByteChannelIO[F, JVMByteBufferModule[F]] {

  lazy val writeIO = new JVMByteChannelWriteIO[F](sbc, tw)
  def write(bbioIn: JVMByteBufferModule[F]#IORO): F[Int] = writeIO.write(bbioIn)
}
