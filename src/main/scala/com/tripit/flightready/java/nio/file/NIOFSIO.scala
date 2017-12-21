package com.tripit.flightready.java.nio.file

import scala.language.higherKinds

import java.nio.file.Files
import java.nio.file.attribute.FileTime

import com.tripit.flightready.integration.category.FlatMap
import com.tripit.flightready.integration.effect.{ThunkWrap, Bracket}
import com.tripit.flightready.integration.streaming.ResourceSafety

import com.tripit.flightready.java.io.InputStreamIO
import com.tripit.flightready.java.nio.{NIOByteBufferModule, NIOSeekableByteChannelIO, SeekableByteChannelReadIO}


object NIOFSIO {
  class Module[F[_]] extends FSIO.Module[F] {
    def fsIO: FSIO[F, NIOFSIO.Module[F]] = ???

    type P = NIOFSPathTypes#P

    type ByteBufferIOMod = NIOByteBufferModule[F]
    def byteBufferModule: ByteBufferIOMod = ???
  }
}

class NIOFSReadIO[F[_]](tw: ThunkWrap[F]) extends FSReadIO[F, NIOFSIO.Module[F]] {
  type P = NIOFSIO.Module[F]#P

  def isSameFile(pl: P, pr: P): F[Boolean] = tw.wrap(Files.isSameFile(pl, pr))

  def exists(p: P): F[Boolean] = tw.wrap(Files.exists(p))
  def notExists(p: P): F[Boolean] = tw.wrap(Files.notExists(p))

  def isDirectory(p: P): F[Boolean] = tw.wrap(Files.isDirectory(p))
  def isExecutable(p: P): F[Boolean] = tw.wrap(Files.isExecutable(p))
  def isHidden(p: P): F[Boolean] = tw.wrap(Files.isHidden(p))
  def isReadable(p: P): F[Boolean] = tw.wrap(Files.isReadable(p))
  def isRegularFile(p: P): F[Boolean] = tw.wrap(Files.isRegularFile(p))
  def isSymbolicLink(p: P): F[Boolean] = tw.wrap(Files.isSymbolicLink(p))
  def isWritable(p: P): F[Boolean] = tw.wrap(Files.isWritable(p))

  def size(p: P): F[Long] = tw.wrap(Files.size(p))
  def lastModifiedTime(p: P): F[FileTime] = tw.wrap(Files.getLastModifiedTime(p))

  def readSymbolicLink(p: P): F[String] = tw.wrap(Files.readSymbolicLink(p).toString)
  def probeContentType(p: P): F[String] = tw.wrap(Files.probeContentType(p))

  def readAllBytes(p: P): F[Array[Byte]] = tw.wrap(Files.readAllBytes(p))

  def onInputStreamF[X](p: P)(run: InputStreamIO[F] => F[X]): F[X] = ???
  def onInputStreamS[S[_[_], _], X](p: P)
                                   (s: InputStreamIO[F] => S[F,X])
                                   (implicit rs: ResourceSafety[S,F]): S[F,X] = ???

  type SBCReadIO = SeekableByteChannelReadIO[F, NIOByteBufferModule[F]]

  def onByteChannelROF[X](p: P)
                         (run: SBCReadIO => F[X])
                         (implicit fm: FlatMap[F], brkt: Bracket[F]): F[X] =
    brkt.bracket(
      tw.wrap(new NIOSeekableByteChannelIO(Files.newByteChannel(p), tw))
    )(io => tw.wrap(io.sbc.close))(run)


  def onByteChannelROS[S[_[_], _], X](p: P)
                                     (run: SBCReadIO => S[F, X])
                                     (implicit rs: ResourceSafety[S, F]): S[F, X] =
    rs.bracketSource(
      tw.wrap(new NIOSeekableByteChannelIO(Files.newByteChannel(p), tw))
    )(sbc => tw.wrap(sbc.sbc.close))(run)
}

class NIOFSIO[F[_]] //extends FSIO[F, NIO]
