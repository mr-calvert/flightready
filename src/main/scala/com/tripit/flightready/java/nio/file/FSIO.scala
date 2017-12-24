package com.tripit.flightready.java.nio.file

import java.nio.file.StandardOpenOption

import scala.language.higherKinds
import java.nio.file.attribute.FileTime

import com.tripit.flightready.integration.category.FlatMap
import com.tripit.flightready.integration.effect.Bracket
import com.tripit.flightready.integration.streaming.ResourceSafety
import com.tripit.flightready.java.io.InputStreamIO
import com.tripit.flightready.java.nio.{ByteBufferIO, SeekableByteChannelReadIO, SeekableByteChannelIO}

object FSIO {
  trait Module[F[_]] {
    def fsIO: FSIO[F, _ <: FSIO.Module[F]]

    /** Exchange type for file system paths */
    type P

    // TODO: well actually... move this to a new higher level module encompassing more than just file io
    type ByteBufferIOMod <: ByteBufferIO.Module[F]
    def byteBufferModule: ByteBufferIOMod
  }
}

trait FSIO[F[_], Mod <: FSIO.Module[F]] extends FSReadIO[F, Mod] with FSWriteIO[F, Mod] {
  // TODO: document somewhere about my ambivalence about var args: 1) eta to Seq version works, 2) use of Seq is iffy, 3) seems to actually work
  def copy(src: Mod#P, dst: Mod#P, options: CopyOption*): F[Mod#P]
  def move(src: Mod#P, dst: Mod#P, options: MoveOption*): F[Mod#P]

  def onByteChannelRWF[X](p: Mod#P, openOptions: OpenRWOption*)
                         (run: SeekableByteChannelIO[F, Mod#ByteBufferIOMod] => F[X])
                         (implicit brkt: Bracket[F]): F[X]
  def onByteChannelRWS[S[_[_], _], I, O]
                      (p: Mod#P, openOptions: OpenRWOption*)
                      (run: SeekableByteChannelIO[F, Mod#ByteBufferIOMod] => S[F, I] => S[F, O])
                      (implicit rs: ResourceSafety[S, F]): S[F, O]
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

  // TODO: Something less mutable than an Array??
  def readAllBytes(p: Mod#P): F[Array[Byte]]

  // TODO: encode CharSet without being horrible
  // TODO: figure out the least horrible way to pass on a java list of strings and replace that Nothing
//  def readAllLines(p: Mod#P): F[Nothing]

  // TODO: newBufferedReader... algebra open buffered reader algebra executor and stream interface, needs a smart way of expressing CharSet, should also support byte streaming

  // TODO: add OpenOptions parameter
  def onInputStreamF[X](p: Mod#P)(run: InputStreamIO[F] => F[X])(implicit brkt: Bracket[F]): F[X]
  def onInputStreamS[S[_[_], _], X](p: Mod#P)
                                   (s: InputStreamIO[F] => S[F, X])
                                   (implicit rs: ResourceSafety[S, F]): S[F, X]

  def onByteChannelROF[X](p: Mod#P, openOption: OpenReadOption*)
                         (run: SeekableByteChannelReadIO[F, Mod#ByteBufferIOMod] => F[X])
                         (implicit brkt: Bracket[F]): F[X]

  def onByteChannelROS[S[_[_], _], X](p: Mod#P, openOption: OpenReadOption*)
                                     (run: SeekableByteChannelReadIO[F, Mod#ByteBufferIOMod] => S[F, X])
                                     (implicit rs: ResourceSafety[S, F]): S[F, X]

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

  def writeByteArray(p: Mod#P, content: Array[Byte]): F[Mod#P] // TODO: add OpenOptions parameter

  // TODO: writeLines... there might be some quick wins here for non-offensive collections being fed in here

  // TODO: newBufferedWriter... be able to feed it either a naked Writer program or a PrintWriter program

  // TODO: newOutputStream... needs a nice little

  // TODO: make this take a write only view of SeekableByteChannelIO, maybe modified to be append only
  def onByteChannelAppendF[X](p: Mod#P, openOptions: OpenRWOption*)
                             (run: SeekableByteChannelIO[F, Mod#ByteBufferIOMod] => F[X])
                             (implicit brkt: Bracket[F]): F[X]
  def onByteChannelAppendS[S[_[_], _], I, O]
                          (p: Mod#P, openOptions: OpenRWOption*)
                          (run: SeekableByteChannelIO[F, Mod#ByteBufferIOMod] => S[F, I] => S[F, O])
                          (implicit rs: ResourceSafety[S, F]): S[F, O]
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

sealed trait OpenOption {
  def jOO: java.nio.file.OpenOption
}

sealed trait OpenReadOption extends OpenOption
object OpenReadOption {
  case object DeleteOnClose extends OpenReadOption { def jOO = StandardOpenOption.DELETE_ON_CLOSE }
}

sealed trait OpenAppendOption extends OpenOption
object OpenAppendOption {
  case object Create extends OpenAppendOption { def jOO = StandardOpenOption.CREATE }
  case object DeleteOnClose extends OpenAppendOption { def jOO = StandardOpenOption.DELETE_ON_CLOSE }
  case object Sparse extends OpenAppendOption { def jOO = StandardOpenOption.SPARSE }
  case object Sync extends OpenAppendOption { def jOO = StandardOpenOption.SYNC }
  case object DSync extends OpenAppendOption { def jOO = StandardOpenOption.DSYNC }
}

sealed trait OpenRWOption extends OpenOption
object OpenRWOption {
  case object TruncateExisting extends OpenRWOption { def jOO = StandardOpenOption.TRUNCATE_EXISTING }
  case object CreateNew extends OpenRWOption { def jOO = StandardOpenOption.CREATE_NEW }
  case object Create extends OpenRWOption { def jOO = StandardOpenOption.CREATE }
  case object DeleteOnClose extends OpenRWOption { def jOO = StandardOpenOption.DELETE_ON_CLOSE }
  case object Sparse extends OpenRWOption { def jOO = StandardOpenOption.SPARSE }
  case object Sync extends OpenRWOption { def jOO = StandardOpenOption.SYNC }
  case object DSync extends OpenRWOption { def jOO = StandardOpenOption.DSYNC }
}
