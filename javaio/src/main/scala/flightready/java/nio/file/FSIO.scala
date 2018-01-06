package flightready.java.nio.file

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

  // TODO: document the assumption that links never change filsystems
  def readSymbolicLink(p: Mod#P): F[Mod#P]

  def probeContentType(p: Mod#P): F[String] // TODO: can we return something smarter than a String?
}

trait FSWriteIO[F[_], Mod <: FSIO.Module[F]] {
  def createDirectories(p: Mod#P): F[Mod#P]
  def createDirectory(p: Mod#P): F[Mod#P]
  def createFile(f: Mod#P): F[Mod#P]
  def createLink(link: Mod#P, existing: Mod#P): F[Mod#P]
  def createSymbolicLink(link: Mod#P, target: Mod#P): F[Mod#P]

  def delete(p: Mod#P): F[Unit]
  def deleteIfExists(p: Mod#P): F[Boolean]
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


