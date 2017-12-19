package com.tripit.flightready.java.nio.file

import scala.language.higherKinds

import java.nio.file.attribute.FileTime

import com.tripit.flightready.integration.streaming.ResourceSafety
import com.tripit.flightready.java.io.InputStreamIO
import com.tripit.flightready.java.nio.{SeekableChannelReadIO, ByteBufferIO}

object FSIO {
  trait Module[F[_]] extends FSPathTypes {
    def fsDirIO: FSIO[F, this.type]

    // TODO: well actually... move this to a new higher level module encompassing more than just file io
    type ByteBufferIOMod <: ByteBufferIO.Module[F]
    def byteBufferModule: ByteBufferIOMod
  }
}

trait FSIO[F[_], Mod <: FSIO.Module[F]] extends FSReadIO[F, Mod] with FSWriteIO[F, Mod] {
  // TODO: deal with CopyOptions... yuck
  def onByteChannelROF[X](p: Mod#P)(run: SeekableChannelReadIO[F, Mod#ByteBufferIOMod] => F[X]): F[X]
  def onByteChannelROS[S[_[_], _], X](p: Mod#P)
                                     (run: SeekableChannelReadIO[F, Mod#ByteBufferIOMod] => S[F, X])
                                     (implicit rs: ResourceSafety[S, F]): S[F, X]

  // TODO: add RW versions

}

trait FSWriteIO[F[_], Mod <: FSIO.Module[F]] {
  // TODO: doc comment including link back to Files method doc
  def copy(src: Mod#P, dst: Mod#P, options: CopyOption*): F[Mod#P]

  // TODO: doc comment including link back to Files method doc
  def move(src: Mod#P, dst: Mod#P, options: MoveOption*): F[Mod#P]

  // TODO: doc comment including link back to Files method doc
  def createDirectories(p: Mod#P): F[Mod#P] // TODO: add FileAttribute parameter

  // TODO: doc comment including link back to Files method doc
  def createDirectory(p: Mod#P): F[Mod#P] // TODO: add FileAttribute parameter

  // TODO: doc comment including link back to Files method doc
  def createFile(f: Mod#P): F[Mod#P] // TODO: add FileAttribute parameter

  // TODO: doc comment including link back to Files method doc
  def createLink(link: Mod#P, existing: Mod#P): F[Mod#P]

  // TODO: doc comment including link back to Files method doc
  def createSymbolicLink(link: Mod#P, target: Mod#P): F[Mod#P] // TODO: add FileAttributes parameter

  // TODO: createTempDirectory methods
  // TODO: createTempFile methods

  // TODO: doc comment including link back to Files method doc
  def delete(p: Mod#P): F[Unit]

  // TODO: doc comment including link back to Files method doc
  def deleteIfExists(p: Mod#P): F[Boolean]

  // TODO: setAttribute... straightforward, but man the native types here scream for help

  // TODO: setLastModifiedTime... straightfoward IF we decide we're ok enshrining FileTime

  // TODO: setOwner... oh man... more UserPrinciple scariness

  // TODO: setPosixFilePermissions... depends on PosixFilePermissions

  def writeByteArray(p: Mod#P, content: Array[Byte]) // TODO: add OpenOptions parameter

  // TODO: writeLines... there might be some quick wins here for non-offensive collections being fed in here

  // TODO: newBufferedWriter... be able to feed it either a naked Writer program or a PrintWriter program

  // TODO: newOutputStream... needs a nice little
}

trait FSReadIO[F[_], Mod <: FSIO.Module[F]] {
  // TODO: add java.nio.file.Path methods that do IO to operate on Paths

  // TODO: doc comment including link back to Files method doc
  def isSameFile(pl: Mod#P, pr: Mod#P): F[Boolean]

  // TODO: doc comment including link back to Files method doc
  def exists(p: Mod#P): F[Boolean] // TODO: add link options parameter

  // TODO: doc comment including link back to Files method doc
  def notExists(p: Mod#P): F[Boolean] // TODO: add LinkOPtions parameter

  // TODO: doc comment including link back to Files method doc
  def isDirectory(p: Mod#P): F[Boolean] // TODO: add LinkOption parameter

  // TODO: doc comment including link back to Files method doc
  def readSymbolicLink(p: Mod#P): F[Mod#P]

  // TODO: doc comment including link back to Files method doc
  def isExecutable(p: Mod#P): F[Boolean]

  // TODO: doc comment including link back to Files method doc
  def isHidden(p: Mod#P): F[Boolean]

  // TODO: doc comment including link back to Files method doc
  def isReadable(p: Mod#P): F[Boolean]

  // TODO: doc comment including link back to Files method doc
  def isRegularFile(p: Mod#P): F[Boolean] // TODO: add LinkOption parameter

  // TODO: doc comment including link back to Files method doc
  def isSymbolicLink(p: Mod#P): F[Boolean]

  // TODO: doc comment including link back to Files method doc
  def isWritable(p: Mod#P): F[Boolean]

  // TODO: doc comment including link back to Files method doc
  def size(p: Mod#P): F[Long]

  // TODO: doc comment including link back to Files method doc
  // TODO: wrap FileTime so as to hide all Java types from interfaces... we'd like to play nice with non java environments
  // TODO: add LinkOptions parameter
  def lastModifiedTime(p: Mod#P): F[FileTime]

  // TODO: getOwner... got to figure out to wrap the whole `Principle` thing

  // TODO: getAttribute.. man this thing is wierd, not sure at all what the in and out encodings are

  // TODO: getFileAttributeView... this looks usefulish, like we could wrap it into something non horrible

  // TODO: getPosixFilePermissions... figure out how to wrap PosixFilePermissions

  // TODO: readAttributes... yeah...??

  // TODO: newDirectoryStream... once created it's just an iterator, this can be a first draft of a streaming approach

  // TODO: walkFileTree... the $64,000,000 question is if it's possible to do an FP wrapping of a visitor... yes if your require an "Unsafe" typeclass for the effect type and return Unit... harder if you want to model building a return value because that value then HAS to be mutable

  // TODO: doc comment including link back to Files method doc
  def probeContentType(p: Mod#P): F[String] // TODO: can we return something smarter than a String?

  // TODO: doc comment including link back to Files method doc
  def readAllBytes(p: Mod#P): F[Array[Byte]]

  // TODO: doc comment including link back to Files method doc
  // TODO: encode CharSet without being horrible
  // TODO: figure out the least horrible way to pass on a java list of strings and replace that Nothing
  def readAllLines(p: Mod#P): F[Nothing]

  // TODO: newBufferedReader... algebra open buffered reader algebra executor and stream interface, needs a smart way of expressing CharSet, should also support byte streaming

  // TODO: doc comment including link back to Files method doc and discussion about FP semantics retrofit
  // TODO: add OpenOptions parameter
  def onInputStreamF[X](p: Mod#P)(run: InputStreamIO[F] => F[X]): F[X]
  def onInputStreamS[S[_[_], _], X](p: Mod#P)(s: InputStreamIO[F] => S[F, X])(implicit rs: ResourceSafety[S, F]): S[F, X]

  // TODO: getFileStore... a naked FileStore is a bad thing so it too needs to be wrapped, question is do we make it opaque and provide an algebra or do we inject a FileStore algebra inside? Or both
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