package flightready.java.nio.file

import scala.util.control.NonFatal

import flightready.IsoImmutableUnsafe
import flightready.integration.category.{Order, FlatMap}
import flightready.integration.effect.{PureWrap, CatchWrap}
import flightready.java.JVMA


class JVMFSPathLogic[F[_]](implicit cw: CatchWrap[F], fm: FlatMap[F]) extends JVMFSPath[F] with FSPathLogic[F, JVMA] {
  import JVMFSPath.{ asPath, tag }
//  def path(p: String): F[P] =
//    cw(tag(fs.getPath(p)))

  def resolve[D](base: P[D], other: String): F[P[D]] =
    cw(tag(base.resolve(other)))

  def resolveSibling[D](base: P[D], other: String): F[P[D]] =
    cw(tag(base.resolveSibling(other)))

  def relativize[D](base: P[D], full: P[D]): F[P[D]] =
    cw(tag(base.relativize(full)))

  def parent[D](p: P[D]): F[P[D]] =
    failNull(FSPathLogic.NoParent, tag(p.getParent))

  def filename[D](p: P[D]): F[P[D]] =
    failNull(FSPathLogic.NoFilename, tag(p.getFileName))

  def name[D](idx: Int, p: P[D]): F[P[D]] =
    cw(tag(p.getName(idx)))

  def subpath[D](p: P[D], start: Int, end: Int): F[P[D]] =
    cw(tag(p.subpath(start, end)))

  def startsWith(base: P[_], prefix: String): F[Boolean] =
    cw(base.startsWith(prefix))

  def endsWith(base: P[_], suffix: String): F[Boolean] =
    cw(base.endsWith(suffix))

  private[this] def failNull[X](failure: => Exception, x: => X): F[X] =
    fm.flatMap(
      cw {
        val mX = x
        if (mX == null) throw failure
        else cw(x)
      }
    )(identity)
}


object JVMFSPath {
  import java.nio.file.Path

  type P[D] = FSPath.P[JVMA, D]

  implicit def jvmFSPath[F[_]](implicit pw: PureWrap[F]): FSPath[F, JVMA] = new JVMFSPath[F]

  def isoPath[D]: IsoImmutableUnsafe[P[D], Path] = 
    new IsoImmutableUnsafe[P[D], Path] {
      def toOpaque(p: Path) = tag(p)
      def toImmutable(p: P[D]) = asPath(p)
    }

  private[java] def tagCheck[D](in: P[D], derived: Path): P[D] = {
    if (in.getFileSystem == derived.getFileSystem) 
      tag(in)
    else 
      throw new Exception("illegal filesystem change")
  }

  implicit private[java] def asPath(p: P[_]): Path = p.asInstanceOf[Path]
  private[java] def tag[D](p: Path): P[D] = {
    import flightready.util.TaggedNewtype.{ Tagged, @@ }
    p.asInstanceOf[Path @@ Tagged[FSPath.PathTag[JVMA, D]]]
  }
}

class JVMFSPath[F[_]](implicit pw: PureWrap[F]) extends FSPath[F, JVMA] {
  import JVMFSPath.{ asPath, tag }

  def string(p: P[_]): F[String] = pw(p.toString)

  def orderTC[D]: Order[P[D]] =
    new Order[P[D]] {
      def compare(l: => P[D], r: => P[D]): Int = l.compareTo(r)
    }

  def resolve[D](base: P[D], other: P[D]): F[P[D]] =
    pw(tag(base.resolve(other)))

  def resolveSibling[D](base: P[D], other: P[D]): F[P[D]] =
    pw(tag(base.resolveSibling(other)))

  def normalize[D](p: P[D]): F[P[D]] =
    pw(tag(p.normalize))

  def subpathOption[D](p: P[D], start: Int, end: Int): F[Option[P[D]]] =
    noneException(tag(p.subpath(start, end)))

  def parentOption[D](p: P[D]): F[Option[P[D]]] =
    pw(Option(tag(p.getParent)))

  def filenameOption[D](p: P[D]): F[Option[P[D]]] =
    pw(Option(tag(p.getFileName)))

  def nameOption[D](idx: Int, p: P[D]): F[Option[P[D]]] =
    noneException(tag(p.getName(idx)))

  def isAbsolute(p: P[_]): F[Boolean] =
    pw(p.isAbsolute)

  def rootOption[D](p: P[D]): F[Option[P[D]]] =
    pw(Option(tag(p.getRoot)))

  def nameCount(p: P[_]): F[Int] =
    pw(p.getNameCount)

  def startsWith[D](base: P[D], prefix: P[D]): F[Boolean] =
    pw(base.startsWith(prefix))

  def endsWith[D](base: P[D], suffix: P[D]): F[Boolean] =
    pw(base.endsWith(suffix))

  def noneException[X](x: => X): F[Option[X]] =
    pw {
      try {
        Option(x)
      } catch {
        case NonFatal(_) => None
      }
    }
}
