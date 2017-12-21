package com.tripit.flightready.java.nio.file

import java.nio.file.Files
import java.nio.file.attribute.FileTime

import com.tripit.flightready.ThunkWrap
import com.tripit.flightready.integration.category.FlatMap
import com.tripit.flightready.integration.streaming.ResourceSafety

import scala.language.higherKinds
import com.tripit.flightready.java.io.InputStreamIO
import com.tripit.flightready.java.nio.{NIOByteBufferModule, SeekableByteChannelReadIO, NIOSeekableByteChannelIO}


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
//  def readAllLines(p: P): F[Nothing] = ???

  def onInputStreamF[X](p: P)(run: InputStreamIO[F] => F[X]): F[X] = ???
  def onInputStreamS[S[_[_], _], X](p: P)
                                   (s: InputStreamIO[F] => S[F,X])
                                   (implicit rs: ResourceSafety[S,F]): S[F,X] = ???

  def onByteChannelROF[X](p: P)
                         (run: SeekableByteChannelReadIO[F, NIOByteBufferModule[F]] => F[X])
                         (implicit fm: FlatMap[F]): F[X] =
    fm.flatMap(
      tw.wrap(new NIOSeekableByteChannelIO(Files.newByteChannel(p), tw))
    )(run)

}

class NIOFSIO[F[_]] //extends FSIO[F, NIO]
