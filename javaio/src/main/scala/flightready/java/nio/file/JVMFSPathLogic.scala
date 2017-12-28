package flightready.java.nio.file

import scala.language.higherKinds
import scala.util.control.NonFatal
import java.nio.file.{FileSystem, Path}

import flightready.IsoImmutableUnsafe
import flightready.integration.category.{Order, FlatMap}
import flightready.integration.effect.{ThunkWrap, PureWrap}


object NIOFSPathLogic {
  def forDefaultFS[F[_]](tw: ThunkWrap[F], fm: FlatMap[F]): IsoFSPathLogic.Module[F] =
    NIOFSPathLogic[F](JVMFSPathTypes.default, tw, fm)

  def apply[F[_]](fsTypes: JVMFSPathTypes, tw: ThunkWrap[F], fm: FlatMap[F]): IsoFSPathLogic.Module[F] =
    new IsoFSPathLogic.Module[F] {
      type FS = fsTypes.FS
      type P = fsTypes.P

      val fsPathLogic: FSPathLogic[F, P] = new NIOFSPathLogic[F, P](fsTypes.fs, tw, fm)
      val isoImmutableUnsafe: IsoImmutableUnsafe[fsTypes.P, Path] =
        new IsoImmutableUnsafe[fsTypes.P, Path] {
          def toOpaque(p: Path): fsTypes.P = fsTypes.tag(p)
          def toImmutable(p: fsTypes.P): Path = p
        }
    }

  // TODO: make a real exception
  // TODO: test that the filesystem equality check is something like functional
  /** Not exactly typesafe hack to retag raw [[Path]] instances.
    *
    * [[NIOFSIO]] has some operations that create new `Path`
    * instances but doesn't have access to the tag method used here.
    * Only operations expected to produce a Path in the same
    * filesystem as its `in` parameter should use this hack. Out of
    * pure healthy paranoia this hack then checks the `derived`
    * `Path` really is in the expected filesystem. Thus preserving
    * the Mod#P == filesystem equivalence class relationship.
    */
  private[java] def tagCheck[P <: Path](in: P, derived: Path): P =
    if (in.getFileSystem == derived.getFileSystem) derived.asInstanceOf[P]
    else throw new Exception("illegal filesystem change")
}

/** Interprets [[FSPathLogic]] by deference to
  * [[java.nio.file.FileSystem]] and friends. */
// TODO: introduce a SuspendWrap typeclass, or maybe just require FlatMap
class NIOFSPathLogic[F[_], P <: Path](val fs: FileSystem, tw: ThunkWrap[F], fm: FlatMap[F])
      extends NIOFSPath[F, P](tw) with FSPathLogic[F, P] {

  def path(p: String): F[P] =
    tw(tag(fs.getPath(p)))

  def resolve(base: P, other: String): F[P] =
    tw(tag(base.resolve(other)))

  def resolveSibling(base: P, other: String): F[P] =
    tw(tag(base.resolveSibling(other)))

  def relativize(base: P, full: P): F[P] =
    tw(tag(base.relativize(full)))

  def parent(p: P): F[P] =
    failNull(FSPathLogic.NoParent, tag(p.getParent))

  def filename(p: P): F[P] =
    failNull(FSPathLogic.NoFilename, tag(p.getFileName))

  def name(idx: Int, p: P): F[P] =
    tw(tag(p.getName(idx)))

  def subpath(p: P, start: Int, end: Int): F[P] =
    tw(tag(p.subpath(start, end)))

  def startsWith(base: P, prefix: String): F[Boolean] =
    tw(base.startsWith(prefix))

  def endsWith(base: P, suffix: String): F[Boolean] =
    tw(base.endsWith(suffix))

  private[this] def failNull[X](failure: => Exception, x: => X): F[X] =
    fm.flatMap(
      tw {
        val mX = x
        if (mX == null) throw failure
        else tw(x)
      }
    )(identity)
}


object NIOFSPath {
  def forDefaultFS[F[_]](tw: ThunkWrap[F]): IsoFSPath.Module[F] = NIOFSPath[F](JVMFSPathTypes.default, tw)

  def apply[F[_]](fsTypes: JVMFSPathTypes, tw: ThunkWrap[F]): IsoFSPath.Module[F] =
    new IsoFSPath.Module[F] {
      type FS = FileSystem
      type P = fsTypes.P

      val fsPathIO: FSPath[F, fsTypes.P] = new NIOFSPath[F, fsTypes.P](tw)
      val isoImmutableUnsafe: IsoImmutableUnsafe[fsTypes.P, Path] =
        new IsoImmutableUnsafe[fsTypes.P, Path] {
          def toOpaque(p: Path): fsTypes.P = fsTypes.tag(p)
          def toImmutable(p: fsTypes.P): Path = p
        }
    }
}

class NIOFSPath[F[_], P <: Path](pw: PureWrap[F]) extends FSPath[F, P] {
  def string(p: P): F[String] = pw(p.toString)

  def orderTC: Order[P] =
    new Order[P] {
      def compare(l: => P, r: => P): Int = l.compareTo(r)
    }

  def resolve(base: P, other: P): F[P] =
    pw(tag(base.resolve(other)))

  def resolveSibling(base: P, other: P): F[P] =
    pw(tag(base.resolveSibling(other)))

  def normalize(p: P): F[P] =
    pw(tag(p.normalize))

  def subpathOption(p: P, start: Int, end: Int): F[Option[P]] =
    noneException(tag(p.subpath(start, end)))

  def parentOption(p: P): F[Option[P]] =
    pw(Option(tag(p.getParent)))

  def filenameOption(p: P): F[Option[P]] =
    pw(Option(tag(p.getFileName)))

  def nameOption(idx: Int, p: P): F[Option[P]] =
    noneException(tag(p.getName(idx)))

  def isAbsolute(p: P): F[Boolean] =
    pw(p.isAbsolute)

  def rootOption(p: P): F[Option[P]] =
    pw(Option(tag(p.getRoot)))

  def nameCount(p: P): F[Int] =
    pw(p.getNameCount)

  def startsWith(base: P, prefix: P): F[Boolean] =
    pw(base.startsWith(prefix))

  def endsWith(base: P, suffix: P): F[Boolean] =
    pw(base.endsWith(suffix))

  def noneException[X](x: => X): F[Option[X]] =
    pw {
      try {
        Option(x)
      } catch {
        case NonFatal(e) => None
      }
    }

  def tag(p: Path): P = p.asInstanceOf[P]
}
