package flightready.java.nio.file

import java.nio.file.{LinkOption, Files}
import java.nio.file.attribute.FileTime

import flightready.integration.effect.ThunkWrap
import flightready.java.nio._


object JVMFSIO {
  class Module[F[_]] extends FSIO.Module[F] {
    def fsIO: FSIO[F, JVMFSIO.Module[F]] = ???

    type P = JVMFSPathTypes#P

    type ByteBufferIOMod = JVMByteBufferModule[F]
    def byteBufferModule: ByteBufferIOMod = ???
  }
}

class JVMFSIO[F[_]](val tw: ThunkWrap[F])
      extends JVMFSReadIO[F](tw) with FSIO[F, JVMFSIO.Module[F]] with JVMFSWriteIOImpl[F] {

  override type P = JVMFSIO.Module[F]#P

  def copy(src: P, dst: P, options: CopyOption*): F[P] =
    tw(
      JVMFSPathLogic.tagCheck(dst, Files.copy(src, dst))
    )

  def move(src: P, dst: P, options: MoveOption*): F[P] =
    tw(
      JVMFSPathLogic.tagCheck(dst, Files.move(src, dst))
    )
}

class JVMFSReadIO[F[_]](tw: ThunkWrap[F]) extends FSReadIO[F, JVMFSIO.Module[F]] {
  type P = JVMFSIO.Module[F]#P

  def realPath(p: P, followLinks: Boolean): F[P] =
    tw(
      JVMFSPathLogic.tagCheck(
        p,
        p.toRealPath(linkOptions(followLinks): _*)
      )
    )

  def absolutePath(p: P): F[P] =
    tw(
      JVMFSPathLogic.tagCheck(p, p.toAbsolutePath)
    )

  def isSameFile(pl: P, pr: P): F[Boolean] = tw(Files.isSameFile(pl, pr))

  def exists(p: P, followLinks: Boolean): F[Boolean] =
    tw(Files.exists(p, linkOptions(followLinks): _*))

  def notExists(p: P, followLinks: Boolean): F[Boolean] =
    tw(Files.notExists(p, linkOptions(followLinks): _*))

  def isDirectory(p: P, followLinks: Boolean): F[Boolean] =
    tw(Files.isDirectory(p, linkOptions(followLinks): _*))

  def isExecutable(p: P): F[Boolean] = tw(Files.isExecutable(p))
  def isHidden(p: P): F[Boolean] = tw(Files.isHidden(p))
  def isReadable(p: P): F[Boolean] = tw(Files.isReadable(p))

  def isRegularFile(p: P, followLinks: Boolean): F[Boolean] =
    tw(Files.isRegularFile(p, linkOptions(followLinks): _*))

  def isSymbolicLink(p: P): F[Boolean] = tw(Files.isSymbolicLink(p))
  def isWritable(p: P): F[Boolean] = tw(Files.isWritable(p))

  def size(p: P): F[Long] = tw(Files.size(p))
  def lastModifiedTime(p: P, followLinks: Boolean): F[FileTime] =
    tw(Files.getLastModifiedTime(p, linkOptions(followLinks): _*))

  def readSymbolicLink(p: P): F[P] =
    tw(
      JVMFSPathLogic.tagCheck(p, Files.readSymbolicLink(p))
    )

  def probeContentType(p: P): F[String] = tw(Files.probeContentType(p))


  private[this] def linkOptions(followLinks: Boolean): List[LinkOption] =
    if (followLinks) List()
    else List(LinkOption.NOFOLLOW_LINKS)
}

trait JVMFSWriteIOImpl[F[_]] extends FSWriteIO[F, JVMFSIO.Module[F]] {
  type P = JVMFSIO.Module[F]#P

  def tw: ThunkWrap[F]

  def createDirectories(p: P): F[P] =
    tw(
      JVMFSPathLogic.tagCheck(p, Files.createDirectories(p))
    )

  def createDirectory(p: P): F[P] =
    tw(
      JVMFSPathLogic.tagCheck(p, Files.createDirectory(p))
    )

  def createFile(f: P): F[P] =
    tw(
      JVMFSPathLogic.tagCheck(f, Files.createFile(f))
    )

  def createLink(link: P, existing: P): F[P] =
    tw(
      JVMFSPathLogic.tagCheck(link, Files.createLink(link, existing))
    )

  def createSymbolicLink(link: P, target: P): F[P] =
    tw(
      JVMFSPathLogic.tagCheck(link, Files.createSymbolicLink(link, target))
    )

  def delete(p: P): F[Unit] = tw(Files.delete(p))
  def deleteIfExists(p: P): F[Boolean] = tw(Files.deleteIfExists(p))


}

class JVMFSWriteIO[F[_]](val tw: ThunkWrap[F]) extends JVMFSWriteIOImpl[F]
