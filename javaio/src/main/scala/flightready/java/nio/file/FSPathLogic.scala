package flightready.java.nio.file

import flightready.integration.category.Order
import flightready.util.TaggedNewtype.{ Tagged, @@ }


object FSPathLogic {
  case object NoParent extends Exception
  case object NoFilename extends Exception
}

/** Manipulates Strings and paths in the context of a specific file
  * system's path semantics.
  *
  * Regarding the name [[FSPathLogic]], the usual "IO" suffix has
  * been replaced with "Logic" to denote this algebra defines only
  * pure computations.
  *
  * This algebra largely recapitulates the [[java.nio.file.Path]]
  * interface in all its messy glory. `Path` is NOT a model of clean
  * orthogonality, but we've resisted the urge to clean up its
  * interface as [[java.nio.file.FileSystem]]s have such great leeway
  * we could never be sure any elided methods were truly redundant.
  *
  * `Path` operations not included here include those regarding URIs,
  * creating absolute paths by resolving against the working
  * directory, canonnicalizing against live file systems, converting
  * to/from [[java.io.File]], and watching files for changes. Elided
  * operations involve datatypes we're avoiding and real IO.
  */
trait FSPathLogic[F[_], A] extends FSPath[F, A] {
  /** Lift a String into a P, failing if the file system doesn't like
    * the cut of its jib. */
  // TODO: Move this to a brand new Paths algebra, may also come from FileSystem algebra
//  def path(p: String): F[P]

  /** Required to be identical to converting `other` to `P` via `path`
    * then passing it to `FSPath.resolve`.
    *
    * Fails only if `other` is unable to be converted to `P`. */
  def resolve[D](base: P[D], other: String): F[P[D]]

  /** Required to be identical to converting `other` to `P` via `path`
    * then passing it to `FSPath.resolveSibling`.
    *
    * Fails only if `other` is unable to be converted to `P`.
    */
  def resolveSibling[D](base: P[D], other: String): F[P[D]]

  /** Construct a relative path by removing `base` from the beginning
    * of `full`.
    *
    * When `base` and `full` are equal returns an empty path.
    * Required to invert `resolve` thus...
    * `relativize(p, resolve(p, q)).equals(q)`
    *
    * Fails if `base` and `full` mutually fail to meet arbitrary
    * requirements of the file system.
    */
  def relativize[D](base: P[D], full: P[D]): F[P[D]]

  /** Remove the deepest node from the path. None if `p` has zero or
    * one elements.
    *
    * Fails if there's no parent, usually if `p` is empty or contains
    * only one name.
    */
  def parent[D](p: P[D]): F[P[D]]

  /** Deepest name in the path regardless of whether it's a file or
    * directory.
    *
    * Fails only if p is empty.
    */
  def filename[D](p: P[D]): F[P[D]]

  /** Access path names by 0 based index. Root will be 0.
    *
    * Fails if index is out of range.
    */
  def name[D](idx: Int, p: P[D]): F[P[D]]

  /** Slice a path.
    *
    * Fails if start < 0 || * end <= start ||
    * end > `FSPath.nameCount`
    */
  def subpath[D](p: P[D], start: Int, end: Int): F[P[D]]

  /** Required to be identical to converting prefix to P with `path`
    * the passing it to `FSPath.startsWith`.
    *
    * Fails only if prefix is unable to be converted to `P`.
    */
  def startsWith(base: P[_], prefix: String): F[Boolean]

  /** Required to be identical to converting prefix to P with `path`
    * the passing it to `FSPath.endsWith`.
    *
    * Fails only if prefix is unable to be converted to `P`.
    */
  def endsWith(base: P[_], suffix: String): F[Boolean]
}

object FSPath {
  trait PathTag[A, D]
  type P[A, D] = @@[_, Tagged[PathTag[A, D]]]
}

/** Wrapping the fragment of [[java.nio.file.Path]] that is pure
  * and/(or can reasonably be made) total.
  ***
  * The `Path` API includes a bunch of partial getters where a
  * reasonable interpretation of the docs shows failure can be
  * avoided by querying other methods. Those getters are wrapped as
  * "xxx" in `FSPathLogic` and 'xxxOption' in `FSPath`. The Option
  * versions encode failure in [[Option]], the "require" versions
  * encode failure in `F`.
  *
  * `Option` encodings are avoided where the `Path` API allows non
  * deterministic/arbitrary failures.
  *
  * `F` is allowed to be eager and does NOT need to encode errors.
  * Thus `Id` is allowed.
  */
trait FSPath[F[_], A] {
  type P[D] = FSPath.P[A, D]

  def orderTC[D]: Order[P[D]]

  def string(p: P[_]): F[String]

  /** When `other` is absolute just returns `other`.
    * When `other` is empty just returns `base`.
    * When `other` somehow has a root element but is not absolute
    * it can do anything it wants.
    * When `other` has no root calculates what `other` references
    * using `base` as a starting point.
    */
  def resolve[D](base: P[D], other: P[D]): F[P[D]]

  /** Ideally replaces the deepest name in `base` with `other`.
    * When `base` lacks a parent returns `other` (even if `other`
    * is empty).
    * When `other` is absolute returns `other`.
    * When `other` is empty returns the parent of `base`.
    * Otherwise returns `resolve` on `other` and the parent of
    * `base`.
    */
  def resolveSibling[D](base: P[D], other: P[D]): F[P[D]]

  /** Some system dependent version of removing redundant path names
    *
    * The docs talk a lot about resolving "." and ".." names in Unix
    * file systems.
    */
  def normalize[D](p: P[D]): F[P[D]]

  /** Slice a path.
    *
    * Fails if start < 0 || * end <= start ||
    * end > `FSPath.nameCount`
    */
  def subpathOption[D](p: P[D], start: Int, end: Int): F[Option[P[D]]]

  /** Remove the deepest node from the path. None if `p` has zero or
    * one elements. Same as `FSPathLogic.requireParent`, but uses
    * [[Option]] to encode possible absence of a parent.
    *
    * Returns None no parent, usually if `p` is empty or contains
    * only one name.
    */
  def parentOption[D](p: P[D]): F[Option[P[D]]]

  /** Deepest name in the path regardless of whether it's a file or
    * directory. Same as `FSPathLogic.requireFilename`, but uses
    * [[Option]] to encode possible absence of a filename.
    *
    * Returns None only if p is empty.
    */
  def filenameOption[D](p: P[D]): F[Option[P[D]]]

  /** Access path names by 0 based index. Root will be 0. Returns None
    * if index is out of range. */
  def nameOption[D](idx: Int, p: P[D]): F[Option[P[D]]]

  /** Extract the file system specific root component from this P if
    * it exists. */
  def rootOption[D](p: P[D]): F[Option[P[D]]]

  /** Does `p` uniquely identify a file/directory location all on its
    * own? */
  def isAbsolute(p: P[_]): F[Boolean]

  /** How many names constitute `p`? */
  def nameCount(p: P[_]): F[Int]

  /** Determines if `base` starts with the same names as `prefix`.
    *
    * Only checks whole names. Does something unspecified and file
    * system dependent for corner cases with root elements.
    */
  def startsWith[D](base: P[D], prefix: P[D]): F[Boolean]

  /** Determines if `base` ends with the same names as `suffix`.
    *
    * Only checks whole names. Does something unspecified and file
    * system dependent for corner cases with root elements.
    */
  def endsWith[D](base: P[D], suffix: P[D]): F[Boolean]
}
