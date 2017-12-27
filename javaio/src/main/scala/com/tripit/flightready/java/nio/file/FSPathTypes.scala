package com.tripit.flightready.java.nio.file

/** Establishes opaque file system path types `P` and `PF` scoped
  * within file system semantics determined by `FS`.
  *
  * Inspecting, constructing, and modifying file system paths shall
  * be performed solely through the algebras [[FSPath]] and
  * [[FSPathLogic]]. These algebras take and return explicit path
  * values which are in turn key arguments to various file and
  * file system algebras.
  *
  * By packaging these related types in a trait we grant algebra IO
  * instances and interpreters the power to encode cross algebra
  * type alignment while allowing client code total parametericity in
  * Path type, thus denying client code any dangerous knowledge of
  * underlying unsafe types from the JVM or any other impure file
  * system implementations.
  */
trait FSPathTypes {
  /** Type for reifying file system specific path semantics */
  type FS

  /** Opaque representation of a path */
  type P

  /** Enable kindedness games on P by adding a fake type parameter.
    *
    * P may be modeled as a PF[P] as all P instances are composed of
    * zero or more file/directory names which are themselves
    * instances of `P`.
    *
    * Providing `PF` enables declaration of a `Foldable[PF]`, for
    * instance.
    */
  type PF[X] = P
}
