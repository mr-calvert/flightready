package flightready.java.nio.file

import java.nio.file.{LinkOption, Files}
import java.nio.file.attribute.FileTime

import flightready.integration.effect.ThunkWrap
import flightready.java.JVMA

import flightready.java.nio._


object JVMFSIO {
  class Module[F[_]] extends FSIO.Module[F] {
    def fsIO: FSIO[F, JVMFSIO.Module[F]] = ???

    type P = JVMFSPathTypes#P

    type ByteBufferIOMod = JVMByteBufferModule[F]
    def byteBufferModule: ByteBufferIOMod = ???
  }
}

class JVMFSIO[F[_]](val tw: ThunkWrap[F]) extends JVMFSReadIO[F](tw) with FSIO[F, JVMA] with JVMFSWriteIOImpl[F] {
  import JVMFSPath.asPath

  def copy[DD](src: P[_], dst: P[DD], options: CopyOption*): F[P[DD]] =
    tw(
      JVMFSPath.tagCheck(dst, Files.copy(src, dst))
    )

  def move[DD](src: P[_], dst: P[DD], options: MoveOption*): F[P[DD]] =
    tw(JVMFSPath.tagCheck(dst, Files.move(src, dst)))
}

class JVMFSReadIO[F[_]](tw: ThunkWrap[F]) extends FSReadIO[F, JVMA] {
  import JVMFSPath.asPath

  def realPath[D](p: P[D], followLinks: Boolean): F[P[D]] =
    tw(JVMFSPath.tagCheck(p, p.toRealPath(linkOptions(followLinks): _*)))

  def absolutePath[D](p: P[D]): F[P[D]] =
    tw(JVMFSPath.tagCheck(p, p.toAbsolutePath))

  def isSameFile(pl: P[_], pr: P[_]): F[Boolean] = tw(Files.isSameFile(pl, pr))

  def exists(p: P[_], followLinks: Boolean): F[Boolean] =
    tw(Files.exists(p, linkOptions(followLinks): _*))

  def notExists(p: P[_], followLinks: Boolean): F[Boolean] =
    tw(Files.notExists(p, linkOptions(followLinks): _*))

  def isDirectory(p: P[_], followLinks: Boolean): F[Boolean] =
    tw(Files.isDirectory(p, linkOptions(followLinks): _*))

  def isExecutable(p: P[_]): F[Boolean] = tw(Files.isExecutable(p))
  def isHidden(p: P[_]): F[Boolean] = tw(Files.isHidden(p))
  def isReadable(p: P[_]): F[Boolean] = tw(Files.isReadable(p))

  def isRegularFile(p: P[_], followLinks: Boolean): F[Boolean] =
    tw(Files.isRegularFile(p, linkOptions(followLinks): _*))

  def isSymbolicLink(p: P[_]): F[Boolean] = tw(Files.isSymbolicLink(p))
  def isWritable(p: P[_]): F[Boolean] = tw(Files.isWritable(p))

  def size(p: P[_]): F[Long] = tw(Files.size(p))
  def lastModifiedTime(p: P[_], followLinks: Boolean): F[FileTime] =
    tw(Files.getLastModifiedTime(p, linkOptions(followLinks): _*))

  def readSymbolicLink[D](p: P[D]): F[P[D]] =
    tw(JVMFSPath.tagCheck(p, Files.readSymbolicLink(p)))

  def probeContentType(p: P[_]): F[String] = tw(Files.probeContentType(p))


  private[this] def linkOptions(followLinks: Boolean): List[LinkOption] =
    if (followLinks) List()
    else List(LinkOption.NOFOLLOW_LINKS)
}

trait JVMFSWriteIOImpl[F[_]] extends FSWriteIO[F, JVMA] {
  import JVMFSPath.asPath

  def tw: ThunkWrap[F]

  def createDirectories[D](p: P[D]): F[P[D]] =
    tw(JVMFSPath.tagCheck(p, Files.createDirectories(p)))

  def createDirectory[D](p: P[D]): F[P[D]] =
    tw(JVMFSPath.tagCheck(p, Files.createDirectory(p)))

  def createFile[D](f: P[D]): F[P[D]] =
    tw(JVMFSPath.tagCheck(f, Files.createFile(f)))

  def createLink[D](link: P[D], existing: P[D]): F[P[D]] =
    tw(JVMFSPath.tagCheck(link, Files.createLink(link, existing)))

  def createSymbolicLink[D](link: P[D], target: P[_]): F[P[D]] =
    tw(JVMFSPath.tagCheck(link, Files.createSymbolicLink(link, target)))

  def delete(p: P[_]): F[Unit] = tw(Files.delete(p))
  def deleteIfExists(p: P[_]): F[Boolean] = tw(Files.deleteIfExists(p))
}

class JVMFSWriteIO[F[_]](val tw: ThunkWrap[F]) extends JVMFSWriteIOImpl[F]
