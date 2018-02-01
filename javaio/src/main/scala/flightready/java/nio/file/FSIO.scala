package flightready.java.nio.file

object FSIO {
  trait Module[F[_]] {
    def fsIO: FSIO[F, _ <: FSIO.Module[F]]
  }
}

trait FSIO[F[_], A] extends FSReadIO[F, A] with FSWriteIO[F, A] {
  override type P[D] = FSPath.P[A, D]

  // TODO: document somewhere about my ambivalence about var args: 1) eta to Seq version works, 2) use of Seq is iffy, 3) seems to actually work
  def copy[DD](src: P[_], dst: P[DD], options: CopyOption*): F[P[DD]]
  def move[DD](src: P[_], dst: P[DD], options: MoveOption*): F[P[DD]]

}

// TOOD; doc comments including links back to original method documentation
trait FSReadIO[F[_], A] {
  type P[D] = FSPath.P[A, D]

  def realPath[D](p: P[D], followLinks: Boolean): F[P[D]]

  def absolutePath[D](p: P[D]): F[P[D]]

  def isSameFile(pl: P[_], pr: P[_]): F[Boolean]

  def exists(p: P[_], followLinks: Boolean): F[Boolean]

  def notExists(p: P[_], followLinks: Boolean): F[Boolean] 

  def isDirectory(p: P[_], followLinks: Boolean): F[Boolean]

  def isExecutable(p: P[_]): F[Boolean]

  def isHidden(p: P[_]): F[Boolean]

  def isReadable(p: P[_]): F[Boolean]

  def isRegularFile(p: P[_], followLinks: Boolean): F[Boolean]

  def isSymbolicLink(p: P[_]): F[Boolean]

  def isWritable(p: P[_]): F[Boolean]

  def size(p: P[_]): F[Long]

  // TODO: document rejection of cross FileSystem links due to type safety
  def readSymbolicLink[D](p: P[D]): F[P[D]]

  def probeContentType(p: P[_]): F[String] // TODO: can we return something smarter than a String?
}

trait FSWriteIO[F[_], A] {
  type P[D] = FSPath.P[A, D]

  def createDirectories[D](p: P[D]): F[P[D]]
  def createDirectory[D](p: P[D]): F[P[D]]
  def createFile[D](f: P[D]): F[P[D]]
  def createLink[D](link: P[D], existing: P[D]): F[P[D]]
  def createSymbolicLink[D](link: P[D], target: P[_]): F[P[D]]

  def delete(p: P[_]): F[Unit]
  def deleteIfExists(p: P[_]): F[Boolean]
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


