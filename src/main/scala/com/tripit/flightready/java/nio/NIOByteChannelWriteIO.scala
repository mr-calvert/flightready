package com.tripit.flightready.java.nio

import com.tripit.flightready.{ThunkWrap, IsoMutable, IsoMutableRORW}
import java.nio.channels.{SeekableByteChannel, ReadableByteChannel, WritableByteChannel}

import scala.language.higherKinds

object NIOByteChannelReadIO {
  def isoMutable[F[_]](implicit tw: ThunkWrap[F]): IsoMutable[NIOByteChannelReadIO[F], ReadableByteChannel] =
    new IsoMutable[NIOByteChannelReadIO[F], ReadableByteChannel] {
      def toMutable(io: NIOByteChannelReadIO[F]): ReadableByteChannel = io.rbc
      def toIO(rbc: ReadableByteChannel): NIOByteChannelReadIO[F] =
        new NIOByteChannelReadIO[F](rbc, tw)
    }
}

class NIOByteChannelReadIO[F[_]](private[nio] val rbc: ReadableByteChannel, tw: ThunkWrap[F])
      extends ByteChannelReadIO[F, NIOByteBufferModule[F]] {

  def read(bbioOut: NIOByteBufferModule[F]#IORW): F[Int] =
    tw.wrap(rbc.read(NIOByteBufferModule[F](tw).isoMutableRORW.toMutable(bbioOut)))
}


object NIOByteChannelWriteIO {
  def isoMutable[F[_]](implicit tw: ThunkWrap[F]): IsoMutable[NIOByteChannelWriteIO[F], WritableByteChannel] =
    new IsoMutable[NIOByteChannelWriteIO[F], WritableByteChannel] {
      def toMutable(io: NIOByteChannelWriteIO[F]): WritableByteChannel = io.wbc
      def toIO(wbc: WritableByteChannel): NIOByteChannelWriteIO[F] =
        new NIOByteChannelWriteIO[F](wbc, tw)
    }
}

class NIOByteChannelWriteIO[F[_]](private[nio] val wbc: WritableByteChannel, tw: ThunkWrap[F])
      extends ByteChannelWriteIO[F, NIOByteBufferModule[F]] {

  def write(bbioIn: NIOByteBufferModule[F]#IORO): F[Int] =
    tw.wrap(wbc.write(NIOByteBufferModule[F](tw).isoMutableRORW.toMutable(bbioIn)))
}


object NIOSeekableByteChannelIO {
  // NOTE: no conversion to NIOSeekableByteChannelIO is provided as
  // there is no way to check a SeekableByteChannel to see if it has
  // write permissions. Want a read/write instance? Use one of the
  // safe open methods in FSIO or force the issue by calling
  // `new NIOSeekableByteChannelReadIO` yourself!
  def isoMutableRORW[F[_]](implicit tw: ThunkWrap[F]):
        IsoMutable[NIOSeekableByteChannelReadIO[F], SeekableByteChannel] =

    new IsoMutable[NIOSeekableByteChannelReadIO[F], SeekableByteChannel] {
      def toMutable(io: NIOSeekableByteChannelReadIO[F]): SeekableByteChannel = io.sbc
      def toIO(sbc: SeekableByteChannel): NIOSeekableByteChannelReadIO[F] =
        new NIOSeekableByteChannelReadIO[F](sbc, tw)
    }
}

class NIOSeekableByteChannelReadIO[F[_]](private[nio] val sbc: SeekableByteChannel, tw: ThunkWrap[F])
      extends NIOByteChannelReadIO[F](sbc, tw) with SeekableByteChannelReadIO[F, NIOByteBufferModule[F]] {

  def position: F[Long] = tw.wrap(sbc.position)
  def setPosition(pos: Long): F[Unit] = tw.wrap(sbc.position(pos))
  def size: F[Long] = tw.wrap(sbc.size)
  def truncate(size: Long): F[Unit] = tw.wrap(sbc.truncate(size))
}

class NIOSeekableByteChannelIO[F[_]](sbc: SeekableByteChannel, tw: ThunkWrap[F])
    extends NIOSeekableByteChannelReadIO[F](sbc, tw) with SeekableByteChannelIO[F, NIOByteBufferModule[F]] {

  lazy val writeIO = new NIOByteChannelWriteIO[F](sbc, tw)
  def write(bbioIn: NIOByteBufferModule[F]#IORO): F[Int] = writeIO.write(bbioIn)
}
