package com.tripit.flightready.java.nio

import java.nio.channels.{ReadableByteChannel, WritableByteChannel, SeekableByteChannel}

import com.tripit.flightready.ThunkWrap

import scala.language.higherKinds

class NIOByteChannelReadIO[F[_]](private[nio] val rbc: ReadableByteChannel, tw: ThunkWrap[F])
      extends ByteChannelReadIO[F, NIOByteBufferModule[F]] {

  def read(bbioOut: NIOByteBufferModule[F]#IORW): F[Int] =
    tw.wrap(rbc.read(NIOByteBufferModule[F](tw).isoMutableRORW.toMutable(bbioOut)))
}

class NIOByteChannelWriteIO[F[_]](private[nio] val wbc: WritableByteChannel, tw: ThunkWrap[F])
      extends ByteChannelWriteIO[F, NIOByteBufferModule[F]] {

  def write(bbioIn: NIOByteBufferModule[F]#IORO): F[Int] =
    tw.wrap(wbc.write(NIOByteBufferModule[F](tw).isoMutableRORW.toMutable(bbioIn)))
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
