package com.tripit.flightready.java.nio.file

import scala.language.higherKinds
import java.nio.file.{Files, LinkOption}
import java.nio.file.attribute.FileTime

import com.tripit.flightready.integration.category.FlatMap
import com.tripit.flightready.integration.effect.{ThunkWrap, Bracket}
import com.tripit.flightready.integration.streaming.ResourceSafety
import com.tripit.flightready.java.io.{InputStreamIO, IOInputStreamIO}
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

  def realPath(p: P, followLinks: Boolean): F[P] =
    tw.wrap(
      NIOFSPathLogic.tagCheck(
        p,
        p.toRealPath(linkOptions(followLinks): _*)
      )
    )

  def isSameFile(pl: P, pr: P): F[Boolean] = tw.wrap(Files.isSameFile(pl, pr))

  def exists(p: P, followLinks: Boolean): F[Boolean] =
    tw.wrap(Files.exists(p, linkOptions(followLinks): _*))

  def notExists(p: P, followLinks: Boolean): F[Boolean] =
    tw.wrap(Files.notExists(p, linkOptions(followLinks): _*))

  def isDirectory(p: P, followLinks: Boolean): F[Boolean] =
    tw.wrap(Files.isDirectory(p, linkOptions(followLinks): _*))

  def isExecutable(p: P): F[Boolean] = tw.wrap(Files.isExecutable(p))
  def isHidden(p: P): F[Boolean] = tw.wrap(Files.isHidden(p))
  def isReadable(p: P): F[Boolean] = tw.wrap(Files.isReadable(p))

  def isRegularFile(p: P, followLinks: Boolean): F[Boolean] =
    tw.wrap(Files.isRegularFile(p, linkOptions(followLinks): _*))

  def isSymbolicLink(p: P): F[Boolean] = tw.wrap(Files.isSymbolicLink(p))
  def isWritable(p: P): F[Boolean] = tw.wrap(Files.isWritable(p))

  def size(p: P): F[Long] = tw.wrap(Files.size(p))
  def lastModifiedTime(p: P, followLinks: Boolean): F[FileTime] =
    tw.wrap(Files.getLastModifiedTime(p, linkOptions(followLinks): _*))

  def readSymbolicLink(p: P): F[P] =
    tw.wrap(
      NIOFSPathLogic.tagCheck(p, Files.readSymbolicLink(p))
    )

  def probeContentType(p: P): F[String] = tw.wrap(Files.probeContentType(p))

  def readAllBytes(p: P): F[Array[Byte]] = tw.wrap(Files.readAllBytes(p))


  def onInputStreamF[X](p: P)(run: InputStreamIO[F] => F[X])(implicit brkt: Bracket[F]): F[X] =
    brkt.bracket(newInputStream(p))(_.close)(run)

  def onInputStreamS[S[_[_], _], X](p: P)
                                   (s: InputStreamIO[F] => S[F,X])
                                   (implicit rs: ResourceSafety[S,F]): S[F,X] =
    rs.bracketSource(newInputStream(p))(_.close)(s)

  private[this] def newInputStream(p: P) =
    tw.wrap(new IOInputStreamIO(Files.newInputStream(p), tw))


  type SBCReadIO = SeekableByteChannelReadIO[F, NIOByteBufferModule[F]]

  def onByteChannelROF[X](p: P)
                         (run: SBCReadIO => F[X])
                         (implicit fm: FlatMap[F], brkt: Bracket[F]): F[X] =
    brkt.bracket(newByteChannel(p))(_.close)(run)

  def onByteChannelROS[S[_[_], _], X](p: P)
                                     (run: SBCReadIO => S[F, X])
                                     (implicit rs: ResourceSafety[S, F]): S[F, X] =
    rs.bracketSource(newByteChannel(p))(_.close)(run)

  private[this] def newByteChannel(p: P) =
    tw.wrap(new NIOSeekableByteChannelIO(Files.newByteChannel(p), tw))


  private[this] def linkOptions(followLinks: Boolean): List[LinkOption] =
    if (followLinks) List()
    else List(LinkOption.NOFOLLOW_LINKS)
}

class NIOFSIO[F[_]] //extends FSIO[F, NIO]
