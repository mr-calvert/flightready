package flightready.java.nio.file

import java.nio.file.{FileSystems, FileSystem, Path}

import flightready.util.TaggedNewtype.@@


object JVMFSPathTypes {
  /** Defines a singular `P`, `PF`, and `FS` types scoped to Java's
    * default [[FileSystem]].
    *
    * `default.P` and `default.PF` are dependent types, tied to this
    * instance of NIOFilesystemTypes. All instances of [[FSPath]] and
    * [[FSPathLogic]] built on the default Java `FileSystem` instance
    * should use **this** instance of NIOFilesystemTypes. Doing so
    * will ensure `P` values returned from one `FSPath`/`FSPathLogic`
    * IO instance will type check when passed to other IO instances,
    * so long as they are all built on this `NIOFilesystemTypes`.
    */
  val default = new JVMFSPathTypes(FileSystems.getDefault)
}

/** Wraps a [[FileSystem]] instance and declares a `P` with a
  * dependently typed tag to prevent [[Path]]s from different file
  * systems from being used with the wrong IO instance. */
class JVMFSPathTypes(val fs: FileSystem) extends FSPathTypes {
  trait InstanceTag

  type FS = FileSystem
  type P = Path @@ InstanceTag

  def tag(p: Path): P = p.asInstanceOf[P]
}

