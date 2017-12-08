package com.tripit.flightready.java.nio.file

import java.nio.file.attribute.FileTime

import com.tripit.flightready.integration.streaming.ResourceSafety
import com.tripit.flightready.java.io.InputStreamIO

import scala.language.higherKinds

object FSIO {
  trait Module[F[_]] extends FSPathTypes {
    def fsDirIO: FSIO[F, P]
  }
}

trait FSIO[F[_], P] extends FSReadIO[F, P] with FSWriteIO[F, P] {
  // TODO: newByteChannel... fun but needs a whole ByteBuffer creation and manipulation algebra to be useful
}

trait FSWriteIO[F[_], P] {
  // TODO: doc comment including link back to Files method doc
  def copy(src: P, dst: P): F[P] // TODO: add CopyAttributes parameter

  // TODO: doc comment including link back to Files method doc
  def move(src: P, dst: P): F[P] // TODO: add CopyAttributes parameter

  // TODO: doc comment including link back to Files method doc
  def createDirectories(p: P): F[P] // TODO: add FileAttribute parameter

  // TODO: doc comment including link back to Files method doc
  def createDirectory(p: P): F[P] // TODO: add FileAttribute parameter

  // TODO: doc comment including link back to Files method doc
  def createFile(f: P): F[P] // TODO: add FileAttribute parameter

  // TODO: doc comment including link back to Files method doc
  def createLink(link: P, existing: P): F[P]

  // TODO: doc comment including link back to Files method doc
  def createSymbolicLink(link: P, target: P): F[P] // TODO: add FileAttributes parameter

  // TODO: createTempDirectory methods
  // TODO: createTempFile methods

  // TODO: doc comment including link back to Files method doc
  def delete(p: P): F[Unit]

  // TODO: doc comment including link back to Files method doc
  def deleteIfExists(p: P): F[Boolean]

  // TODO: setAttribute... straightforward, but man the native types here scream for help

  // TODO: setLastModifiedTime... straightfoward IF we decide we're ok enshrining FileTime

  // TODO: setOwner... oh man... more UserPrinciple scariness

  // TODO: setPosixFilePermissions... depends on PosixFilePermissions

  def writeByteArray(p: P, content: Array[Byte]) // TODO: add OpenOptions parameter

  // TODO: writeLines... there might be some quick wins here for non-offensive collections being fed in here

  // TODO: newBufferedWriter... be able to feed it either a naked Writer program or a PrintWriter program

  // TODO: newOutputStream... needs a nice little
}

trait FSReadIO[F[_], P] {
  // TODO: add java.nio.file.Path methods that do IO to operate on Paths

  // TODO: doc comment including link back to Files method doc
  def isSameFile(pl: P, pr: P): F[Boolean]

  // TODO: doc comment including link back to Files method doc
  def exists(p: P): F[Boolean] // TODO: add link options parameter

  // TODO: doc comment including link back to Files method doc
  def notExists(p: P): F[Boolean] // TODO: add LinkOPtions parameter

  // TODO: doc comment including link back to Files method doc
  def isDirectory(p: P): F[Boolean] // TODO: add LinkOption parameter

  // TODO: doc comment including link back to Files method doc
  def readSymbolicLink(p: P): F[P]

  // TODO: doc comment including link back to Files method doc
  def isExecutable(p: P): F[Boolean]

  // TODO: doc comment including link back to Files method doc
  def isHidden(p: P): F[Boolean]

  // TODO: doc comment including link back to Files method doc
  def isReadable(p: P): F[Boolean]

  // TODO: doc comment including link back to Files method doc
  def isRegularFile(p: P): F[Boolean] // TODO: add LinkOption parameter

  // TODO: doc comment including link back to Files method doc
  def isSymbolicLink(p: P): F[Boolean]

  // TODO: doc comment including link back to Files method doc
  def isWritable(p: P): F[Boolean]

  // TODO: doc comment including link back to Files method doc
  def size(p: P): F[Long]

  // TODO: doc comment including link back to Files method doc
  // TODO: wrap FileTime so as to hide all Java types from interfaces... we'd like to play nice with non java environments
  // TODO: add LinkOptions parameter
  def lastModifiedTime(p: P): F[FileTime]

  // TODO: getOwner... got to figure out to wrap the whole `Principle` thing

  // TODO: getAttribute.. man this thing is wierd, not sure at all what the in and out encodings are

  // TODO: getFileAttributeView... this looks usefulish, like we could wrap it into something non horrible

  // TODO: getPosixFilePermissions... figure out how to wrap PosixFilePermissions

  // TODO: readAttributes... yeah...??

  // TODO: newDirectoryStream... once created it's just an iterator, this can be a first draft of a streaming approach

  // TODO: walkFileTree... the $64,000,000 question is if it's possible to do an FP wrapping of a visitor... yes if your require an "Unsafe" typeclass for the effect type and return Unit... harder if you want to model building a return value because that value then HAS to be mutable

  // TODO: doc comment including link back to Files method doc
  def probeContentType(p: P): F[String] // TODO: can we return something smarter than a String?

  // TODO: doc comment including link back to Files method doc
  def readAllBytes(p: P): F[Array[Byte]]

  // TODO: doc comment including link back to Files method doc
  // TODO: encode CharSet without being horrible
  // TODO: figure out the least horrible way to pass on a java list of strings and replace that Nothing
  def readAllLines(p: P): F[Nothing]

  // TODO: newBufferedReader... algebra open buffered reader algebra executor and stream interface, needs a smart way of expressing CharSet, should also support byte streaming

  // TODO: doc comment including link back to Files method doc and discussion about FP semantics retrofit
  // TODO: add OpenOptions parameter
  def onInputStreamF[X](p: P)(run: InputStreamIO[F] => F[X]): F[X]
  def onInputStreamS[S[_[_], _], X](p: P)(s: InputStreamIO[F] => S[F, X])(implicit rs: ResourceSafety[S, F]): S[F, X]

  // TODO: getFileStore... a naked FileStore is a bad thing so it too needs to be wrapped, question is do we make it opaque and provide an algebra or do we inject a FileStore algebra inside? Or both
}
