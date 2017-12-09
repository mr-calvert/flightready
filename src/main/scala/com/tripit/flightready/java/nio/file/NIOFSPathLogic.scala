package com.tripit.flightready.java.nio.file

import scala.language.higherKinds
import scala.util.control.NonFatal
import java.nio.file.{FileSystem, Path}

import cats.Applicative
import cats.effect.Sync
import com.tripit.flightready.{ThunkWrap, IsoImmutableUnsafe}
import com.tripit.flightready.integration.category.Order


object NIOFSPathLogic {
  def forDefaultFS[F[_]: Sync]: IsoFSPathLogic.Module[F] =
    NIOFSPathLogic[F](NIOFSPathTypes.default)

  def apply[F[_]: Sync](fsTypes: NIOFSPathTypes): IsoFSPathLogic.Module[F] =
    new IsoFSPathLogic.Module[F] {
      type FS = fsTypes.FS
      type P = fsTypes.P

      val fsPathLogic: FSPathLogic[F, P] = new NIOFSPathLogic[F, P](fsTypes.fs)
      val isoImmutableUnsafe: IsoImmutableUnsafe[fsTypes.P, Path] =
        new IsoImmutableUnsafe[fsTypes.P, Path] {
          def toOpaque(p: Path): fsTypes.P = fsTypes.tag(p)
          def toImmutable(p: fsTypes.P): Path = p
        }
    }
}

/** Interprets [[FSPathLogic]] by deference to
  * [[java.nio.file.FileSystem]] and friends. */
class NIOFSPathLogic[F[_]: Sync, P <: Path](val fs: FileSystem)
      extends NIOFSPath[F, P](ThunkWrap.DelayInSync[F]) with FSPathLogic[F, P] {

  def path(p: String): F[P] =
    Sync[F].delay(tag(fs.getPath(p)))

  def resolve(base: P, other: String): F[P] =
    Sync[F].delay(tag(base.resolve(other)))

  def resolveSibling(base: P, other: String): F[P] =
    Sync[F].delay(tag(base.resolveSibling(other)))

  def relativize(base: P, full: P): F[P] =
    Sync[F].delay(tag(base.relativize(full)))

  def parent(p: P): F[P] =
    failNull(FSPathLogic.NoParent, tag(p.getParent))

  def filename(p: P): F[P] =
    failNull(FSPathLogic.NoFilename, tag(p.getFileName))

  def name(idx: Int, p: P): F[P] =
    Sync[F].delay(tag(p.getName(idx)))

  def subpath(p: P, start: Int, end: Int): F[P] =
    Sync[F].delay(tag(p.subpath(start, end)))

  def startsWith(base: P, prefix: String): F[Boolean] =
    Sync[F].delay(base.startsWith(prefix))

  def endsWith(base: P, suffix: String): F[Boolean] =
    Sync[F].delay(base.endsWith(suffix))

  def failNull[X](failure: => Exception, x: => X): F[X] =
    Sync[F].suspend {
      val mX = x
      if (mX == null) Sync[F].raiseError(failure)
      else Sync[F].pure(x)
    }
}


object NIOFSPath {
  def forDefaultFS[F[_]: Applicative]: IsoFSPath.Module[F] = NIOFSPath[F](NIOFSPathTypes.default)

  def apply[F[_]: Applicative](fsTypes: NIOFSPathTypes): IsoFSPath.Module[F] =
    new IsoFSPath.Module[F] {
      type FS = FileSystem
      type P = fsTypes.P

      val fsPathIO: FSPath[F, fsTypes.P] = new NIOFSPath[F, fsTypes.P](ThunkWrap.intoPure[F])
      val isoImmutableUnsafe: IsoImmutableUnsafe[fsTypes.P, Path] = ???
    }
}

class NIOFSPath[F[_], P <: Path](tw: ThunkWrap[F]) extends FSPath[F, P] {
  def string(p: P): F[String] = tw.wrap(p.toString)

  def orderTC: Order[P] =
    new Order[P] {
      def compare(l: => P, r: => P): Int = l.compareTo(r)
    }

  def resolve(base: P, other: P): F[P] =
    tw.wrap(tag(base.resolve(other)))

  def resolveSibling(base: P, other: P): F[P] =
    tw.wrap(tag(base.resolveSibling(other)))

  def normalize(p: P): F[P] =
    tw.wrap(tag(p.normalize))

  def subpathOption(p: P, start: Int, end: Int): F[Option[P]] =
    noneException(tag(p.subpath(start, end)))

  def parentOption(p: P): F[Option[P]] =
    tw.wrap(Option(tag(p.getParent)))

  def filenameOption(p: P): F[Option[P]] =
    tw.wrap(Option(tag(p.getFileName)))

  def nameOption(idx: Int, p: P): F[Option[P]] =
    noneException(tag(p.getName(idx)))

  def isAbsolute(p: P): F[Boolean] =
    tw.wrap(p.isAbsolute)

  def rootOption(p: P): F[Option[P]] =
    tw.wrap(Option(tag(p.getRoot)))

  def nameCount(p: P): F[Int] =
    tw.wrap(p.getNameCount)

  def startsWith(base: P, prefix: P): F[Boolean] =
    tw.wrap(base.startsWith(prefix))

  def endsWith(base: P, suffix: P): F[Boolean] =
    tw.wrap(base.endsWith(suffix))

  def noneException[X](x: => X): F[Option[X]] =
    tw.wrap {
      try {
        Option(x)
      } catch {
        case NonFatal(e) => None
      }
    }

  def tag(p: Path): P = p.asInstanceOf[P]
}
