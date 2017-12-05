package com.tripit.flightready.filesystem

import scala.language.higherKinds

// TODO: combine the comments for the object and the trait and uncomment the object, maybe the top comment moves to package
/** Principled file system path manipulation.
  *
  * Java presents a messy but comprehensive suite of tools for
  * representing and manipulating file system paths. The primary
  * touch point for these tools is [[java.nio.file.Path]] which is at
  * least immutable, but its full of side effects and exceptions.
  * Thus, despite its incumbent position, `Path` is ineligible to
  * appear in FlightReady's interface.
  *
  * `Path` defers all specifics regarding file paths to
  * [[java.nio.file.FileSystem]] objects which opaquely implement
  * system dependent rules. This model prevents us from inferring and
  * formalizing the underlying rules; we must abstract over file
  * systems as inscrutable black boxes. To add further complication
  * the library allows for multiple `FileSystem` instances to be
  * simultaneously active.
  *
  * Surprisingly the Java library's design gives us a small gift;
  * a useful layer of path manipulation methods do NOT declare
  * [[java.io.IOException]] as checked exceptions, which gives us
  * evidence they are only allowed to perform pure computations.
  *
  * This algebra is an attempt to construct a sane interface to
  * Java's path manipulation mess without curtailing any of its
  * generality.
  *
  * As none of these operations perform IO they are only modeled via
  * this EDSL instead of pure functions because of their habit of
  * throwing exceptions on arbitrary inputs. Thus `F` may be eager
  * without any loss of referential transparency. `F` need only
  * model partiality and failure to preserve legality.
  */
object FSPathLogic {
  trait Module[F[_]] extends FSPath.FSTypes {
    def fsPathLogicIO: FSPathLogic[F, P]
  }

  case object NoParent extends Exception
  case object NoFilename extends Exception
}

// TODO: define laws for [[FSPathLogic]] by reading docs
// TODO: use tut for compiled and typechecked code snippets in doc comments
/** Manipulates Strings and paths in the context of a specific file
  * system's semantics.
  *
  * Regarding the name [[FSPathLogic]], the usual "IO" suffix has
  * been replaced with "Logic" to denote this algebra is defines only
  * pure computations. `F` needs to
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
trait FSPathLogic[F[_], P] {
  /** Lift a String into a P, failing if the file system doesn't like
    * the cut of its jib. */
  def path(p: String): F[P]

  /** Required to be identical to converting `other` to `P` via `path`
    * then passing it to `FSPath.resolve`.
    *
    * Fails only if `other` is unable to be converted to `P`. */
  def resolve(base: P, other: String): F[P]

  /** Required to be identical to converting `other` to `P` via `path`
    * then passing it to `FSPath.resolveSibling`.
    *
    * Fails only if `other` is unable to be converted to `P`.
    */
  def resolveSibling(base: P, other: String): F[P]

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
  def relativize(base: P, full: P): F[P]

  /** Remove the deepest node from the path. None if `p` has zero or
    * one elements.
    *
    * Fails if there's no parent, usually if `p` is empty or contains
    * only one name.
    */
  def parent(p: P): F[P]

  /** Deepest name in the path regardless of whether it's a file or
    * directory.
    *
    * Fails only if p is empty.
    */
  def filename(p: P): F[P]

  /** Access path names by 0 based index. Root will be 0.
    *
    * Fails if index is out of range.
    */
  def name(idx: Int, p: P): F[P]

  /** Slice a path.
    *
    * Fails if start < 0 || * end <= start ||
    * end > `FSPath.nameCount`
    */
  def subpath(p: P, start: Int, end: Int): F[P]

  /** Required to be identical to converting prefix to P with `path`
    * the passing it to `FSPath.startsWith`.
    *
    * Fails only if prefix is unable to be converted to `P`.
    */
  def startsWith(base: P, prefix: String): F[Boolean]

  /** Required to be identical to converting prefix to P with `path`
    * the passing it to `FSPath.endsWith`.
    *
    * Fails only if prefix is unable to be converted to `P`.
    */
  def endsWith(base: P, suffix: String): F[Boolean]
}

object FSPath {
  trait Module[F[_]] extends FSTypes {
    def fsPathIO: FSPath[F, P]
  }

  trait FSTypes {
    /** Type for reifying file system specific path semantics */
    type FS

    /** Opaque representation of a path */
    type P

    /** Give `P` a fake type parameter so we can declare `Foldable`
      * instances for it.
      */
    type PF[X] = P
  }
}

/** Wrapping the fragment of [[java.nio.file.Path]] that is pure (or
  * can reasonably be made) total.
  *
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
  * `F` is allowed to be eager and does NOT need to handle errors.
  * Thus `Id` is allowed.
  */
trait FSPath[F[_], P] {
  def string(p: P): F[String]

  /** When `other` is absolute just returns `other`.
    * When `other` is empty just returns `base`.
    * When `other` somehow has a root element but is not absolute
    * it can do anything it wants.
    * When `other` has no root calculates what `other` references
    * using `base` as a starting point.
    */
  def resolve(base: P, other: P): F[P]

  /** Ideally replaces the deepest name in `base` with `other`.
    * When `base` lacks a parent returns `other` (even if `other`
    * is empty).
    * When `other` is absolute returns `other`.
    * When `other` is empty returns the parent of `base`.
    * Otherwise returns `resolve` on `other` and the parent of
    * `base`.
    */
  def resolveSibling(base: P, other: P): F[P]

  /** Some system dependent version of removing redundant path names
    *
    * The docs talk a lot about resolving "." and ".." names in Unix
    * file systems.
    */
  def normalize(p: P): F[P]

  /** Slice a path.
    *
    * Fails if start < 0 || * end <= start ||
    * end > `FSPath.nameCount`
    */
  def subpathOption(p: P, start: Int, end: Int): F[Option[P]]

  /** Remove the deepest node from the path. None if `p` has zero or
    * one elements. Same as `FSPathLogic.requireParent`, but uses
    * [[Option]] to encode possible absence of a parent.
    *
    * Returns None no parent, usually if `p` is empty or contains
    * only one name.
    */
  def parentOption(p: P): F[Option[P]]

  /** Deepest name in the path regardless of whether it's a file or
    * directory. Same as `FSPathLogic.requireFilename`, but uses
    * [[Option]] to encode possible absence of a filename.
    *
    * Returns None only if p is empty.
    */
  def filenameOption(p: P): F[Option[P]]

  /** Access path names by 0 based index. Root will be 0. Returns None
    * if index is out of range. */
  def nameOption(idx: Int, p: P): F[Option[P]]

  /** Extract the file system specific root component from this P if
    * it exists. */
  def rootOption(p: P): F[Option[P]]

  /** Does `p` uniquely identify a file/directory location all on its
    * own? */
  def isAbsolute(p: P): F[Boolean]

  /** How many names constitute `p`? */
  def nameCount(p: P): F[Int]

  /** Determines if `base` starts with the same names as `prefix`.
    *
    * Only checks whole names. Does something unspecified and file
    * system dependent for corner cases with root elements.
    */
  def startsWith(base: P, prefix: P): F[Boolean]

  /** Determines if `base` ends with the same names as `suffix`.
    *
    * Only checks whole names. Does something unspecified and file
    * system dependent for corner cases with root elements.
    */
  def endsWith(base: P, suffix: P): F[Boolean]
}
