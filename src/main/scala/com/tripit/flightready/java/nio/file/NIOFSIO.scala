package com.tripit.flightready.java.nio.file

import scala.language.higherKinds
import java.nio.file.{LinkOption, Files, StandardOpenOption}
import java.nio.file.attribute.FileTime

import com.tripit.flightready.integration.effect.{ThunkWrap, Bracket}
import com.tripit.flightready.integration.streaming.ResourceSafety
import com.tripit.flightready.java.io.{InputStreamIO, IOInputStreamIO}
import com.tripit.flightready.java.nio._


object NIOFSIO {
  class Module[F[_]] extends FSIO.Module[F] {
    def fsIO: FSIO[F, NIOFSIO.Module[F]] = ???

    type P = NIOFSPathTypes#P

    type ByteBufferIOMod = NIOByteBufferModule[F]
    def byteBufferModule: ByteBufferIOMod = ???
  }
}

class NIOFSIO[F[_]](val tw: ThunkWrap[F])
      extends NIOFSReadIO[F](tw) with FSIO[F, NIOFSIO.Module[F]] with NIOFSWriteIOImpl[F] {

  override type P = NIOFSIO.Module[F]#P

  def copy(src: P, dst: P, options: CopyOption*): F[P] =
    tw.wrap(
      NIOFSPathLogic.tagCheck(dst, Files.copy(src, dst))
    )

  def move(src: P, dst: P, options: MoveOption*): F[P] =
    tw.wrap(
      NIOFSPathLogic.tagCheck(dst, Files.move(src, dst))
    )


  type SBCIO = SeekableByteChannelIO[F, NIOByteBufferModule[F]]

  def onByteChannelRWF[X](p: P, openOptions: OpenRWOption*)(run: SBCIO => F[X])(implicit brkt: Bracket[F]): F[X] =
    brkt.bracket(newByteChannelRW(p, openOptions))(_.close)(run)

  def onByteChannelRWS[S[_[_], _], I, O]
                      (p: P, openOptions: OpenRWOption*)
                      (run: SeekableByteChannelIO[F, NIOByteBufferModule[F]] => S[F, I] => S[F, O])
                      (implicit rs: ResourceSafety[S, F]): S[F, O] =

    rs.bracketSink(newByteChannelRW(p, openOptions))(_.close)(run)

  private[this] def newByteChannelRW(p: P, oos: Seq[OpenRWOption]) =
    tw.wrap(
      new NIOSeekableByteChannelIO(
        Files.newByteChannel(
          p,
          StandardOpenOption.READ +: StandardOpenOption.WRITE +: oos.map { _.jOO }: _*
        ),
        tw
      )
    )
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

  def absolutePath(p: P): F[P] =
    tw.wrap(
      NIOFSPathLogic.tagCheck(p, p.toAbsolutePath)
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

  def onByteChannelROF[X](p: P, openOptions: OpenReadOption*)
                         (run: SBCReadIO => F[X])
                         (implicit brkt: Bracket[F]): F[X] =
    brkt.bracket(newByteChannelRO(p, openOptions))(_.close)(run)

  def onByteChannelROS[S[_[_], _], X](p: P, openOptions: OpenReadOption*)
                                     (run: SBCReadIO => S[F, X])
                                     (implicit rs: ResourceSafety[S, F]): S[F, X] =
    rs.bracketSource(newByteChannelRO(p, openOptions))(_.close)(run)

  private[this] def newByteChannelRO(p: P, oos: Seq[OpenReadOption]) =
    tw.wrap(
      new NIOSeekableByteChannelReadIO(
        Files.newByteChannel(
          p,
          StandardOpenOption.READ +: oos.map { _.jOO }: _*
        ),
        tw
      )
    )


  private[this] def linkOptions(followLinks: Boolean): List[LinkOption] =
    if (followLinks) List()
    else List(LinkOption.NOFOLLOW_LINKS)
}

trait NIOFSWriteIOImpl[F[_]] extends FSWriteIO[F, NIOFSIO.Module[F]] {
  type P = NIOFSIO.Module[F]#P

  def tw: ThunkWrap[F]

  def createDirectories(p: P): F[P] =
    tw.wrap(
      NIOFSPathLogic.tagCheck(p, Files.createDirectories(p))
    )

  def createDirectory(p: P): F[P] =
    tw.wrap(
      NIOFSPathLogic.tagCheck(p, Files.createDirectory(p))
    )

  def createFile(f: P): F[P] =
    tw.wrap(
      NIOFSPathLogic.tagCheck(f, Files.createFile(f))
    )

  def createLink(link: P, existing: P): F[P] =
    tw.wrap(
      NIOFSPathLogic.tagCheck(link, Files.createLink(link, existing))
    )

  def createSymbolicLink(link: P, target: P): F[P] =
    tw.wrap(
      NIOFSPathLogic.tagCheck(link, Files.createSymbolicLink(link, target))
    )

  def delete(p: P): F[Unit] = tw.wrap(Files.delete(p))
  def deleteIfExists(p: P): F[Boolean] = tw.wrap(Files.deleteIfExists(p))
  def writeByteArray(f: P, content: Array[Byte]): F[P] =
    tw.wrap(
      NIOFSPathLogic.tagCheck(f, Files.createFile(f))
    )


  def onByteChannelAppendF[X](p: P, openOptions: OpenAppendOption*)
                             (run: ByteChannelWriteIO[F, NIOByteBufferModule[F]] => F[X])
                             (implicit brkt: Bracket[F]): F[X] =
    brkt.bracket(newByteChannelAppend(p, openOptions))(_.close)(run)

  def onByteChannelAppendS[S[_[_], _], I, O]
                          (p: P, openOptions: OpenAppendOption*)
                          (run: ByteChannelWriteIO[F, NIOByteBufferModule[F]] => S[F, I] => S[F, O])
                          (implicit rs: ResourceSafety[S, F]): S[F, O] =
    rs.bracketSink(newByteChannelAppend(p, openOptions))(_.close)(run)

  private[this] def newByteChannelAppend(p: P, oos: Seq[OpenAppendOption]) =
    tw.wrap(
      new NIOByteChannelWriteIO(
        Files.newByteChannel(
          p,
          StandardOpenOption.APPEND +: oos.map { _.jOO }: _*
        ),
        tw
      )
    )

}

class NIOFSWriteIO[F[_]](val tw: ThunkWrap[F]) extends NIOFSWriteIOImpl[F]
