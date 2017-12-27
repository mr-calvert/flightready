package flightready.java.nio.file

import scala.language.higherKinds

import java.nio.file.attribute.FileTime

object FSIO {
  trait Module[F[_]] {
    def fsIO: FSIO[F, _ <: FSIO.Module[F]]

    /** Exchange type for file system paths */
    type P
  }
}

trait FSIO[F[_], Mod <: FSIO.Module[F]] extends FSReadIO[F, Mod] with FSWriteIO[F, Mod] {
  // TODO: document somewhere about my ambivalence about var args: 1) eta to Seq version works, 2) use of Seq is iffy, 3) seems to actually work
  def copy(src: Mod#P, dst: Mod#P, options: CopyOption*): F[Mod#P]
  def move(src: Mod#P, dst: Mod#P, options: MoveOption*): F[Mod#P]

}

// TOOD; doc comments including links back to original method documentation
trait FSReadIO[F[_], Mod <: FSIO.Module[F]] {
  // TODO: figure out watch services, how to wrap

  def realPath(p: Mod#P, followLinks: Boolean): F[Mod#P]

  def absolutePath(p: Mod#P): F[Mod#P]

  def isSameFile(pl: Mod#P, pr: Mod#P): F[Boolean]

  def exists(p: Mod#P, followLinks: Boolean): F[Boolean]

  def notExists(p: Mod#P, followLinks: Boolean): F[Boolean]

  def isDirectory(p: Mod#P, followLinks: Boolean): F[Boolean]

  def isExecutable(p: Mod#P): F[Boolean]

  def isHidden(p: Mod#P): F[Boolean]

  def isReadable(p: Mod#P): F[Boolean]

  def isRegularFile(p: Mod#P, followLinks: Boolean): F[Boolean]

  def isSymbolicLink(p: Mod#P): F[Boolean]

  def isWritable(p: Mod#P): F[Boolean]

  def size(p: Mod#P): F[Long]

  // TODO: wrap FileTime so as to hide all Java types from interfaces... we'd like to play nice with non java environments
  def lastModifiedTime(p: Mod#P, followLinks: Boolean): F[FileTime]

  // TODO: document the assumption that links never change filsystems
  def readSymbolicLink(p: Mod#P): F[Mod#P]

  // TODO: getOwner... got to figure out to wrap the whole `Principle` thing

  // TODO: getAttribute.. man this thing is wierd, not sure at all what the in and out encodings are

  // TODO: getFileAttributeView... this looks usefulish, like we could wrap it into something non horrible

  // TODO: getPosixFilePermissions... figure out how to wrap PosixFilePermissions

  // TODO: readAttributes... yeah...??

  // TODO: newDirectoryStream... once created it's just an iterator, this can be a first draft of a streaming approach

  // TODO: walkFileTree... the $64,000,000 question is if it's possible to do an FP wrapping of a visitor... yes if your require an "Unsafe" typeclass for the effect type and return Unit... harder if you want to model building a return value because that value then HAS to be mutable

  def probeContentType(p: Mod#P): F[String] // TODO: can we return something smarter than a String?



  // TODO: getFileStore... a naked FileStore is a bad thing so it too needs to be wrapped, question is do we make it opaque and provide an algebra or do we inject a FileStore algebra inside? Or both
}

trait FSWriteIO[F[_], Mod <: FSIO.Module[F]] {
  def createDirectories(p: Mod#P): F[Mod#P] // TODO: add FileAttribute parameter
  def createDirectory(p: Mod#P): F[Mod#P] // TODO: add FileAttribute parameter
  def createFile(f: Mod#P): F[Mod#P] // TODO: add FileAttribute parameter
  def createLink(link: Mod#P, existing: Mod#P): F[Mod#P]
  def createSymbolicLink(link: Mod#P, target: Mod#P): F[Mod#P] // TODO: add FileAttributes parameter

  // TODO: createTempDirectory methods
  // TODO: createTempFile methods

  def delete(p: Mod#P): F[Unit]
  def deleteIfExists(p: Mod#P): F[Boolean]

  // TODO: setAttribute... straightforward, but man the native types here scream for help

  // TODO: setLastModifiedTime... straightfoward IF we decide we're ok enshrining FileTime

  // TODO: setOwner... oh man... more UserPrinciple scariness

  // TODO: setPosixFilePermissions... depends on PosixFilePermissions
}


sealed trait CopyOption
object CopyOption {
  case object ReplaceExisting extends CopyOption
  case object CopyAttributes extends CopyOption
  case object NoFollowLinks extends CopyOption
}

sealed trait MoveOption
object MoveOption {
  case object ReplaceExisting extends MoveOption
  case object AtomicMove extends MoveOption
}


