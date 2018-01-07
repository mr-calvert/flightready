package flightready.java.nio.file

import java.nio.file.{StandardOpenOption, Files}

import flightready.integration.collection.JListFoldable
import flightready.integration.effect.{ThunkWrap, Bracket}
import flightready.integration.streaming.ResourceSafety
import flightready.java.io.{InputStreamIO, JVMInputStreamIO}
import flightready.java.nio._
import flightready.java.nio.charset.JVMRequiredCharsetIO

object JVMFileIO {
  class Module[F[_]] extends FileIO.Module[F] {
    def fileIO: FileIO[F, JVMFileIO.Module[F]] = ???

    type P = JVMFSPathTypes#P

    type CS = JVMRequiredCharsetIO[F]

    type ByteBufferIOMod = JVMByteBufferModule[F]
    def byteBufferModule: ByteBufferIOMod = ???
  }
}

class JVMFileIO[F[_]](val tw: ThunkWrap[F])
      extends JVMFileReadIO[F](tw) with FileIO[F, JVMFileIO.Module[F]] with JVMFileWriteIOImpl[F] {

  override type P = JVMFileIO.Module[F]#P
  type SBCIO = SeekableByteChannelIO[F, JVMByteBufferModule[F]]

  def onByteChannelRWF[X](p: P, openOptions: OpenRWOption*)(run: SBCIO => F[X])(implicit brkt: Bracket[F]): F[X] =
    brkt.bracket(newByteChannelRW(p, openOptions))(_.close)(run)

  def onByteChannelRWS[S[_[_], _], I, O]
                      (p: P, openOptions: OpenRWOption*)
                      (run: SeekableByteChannelIO[F, JVMByteBufferModule[F]] => S[F, I] => S[F, O])
                      (implicit rs: ResourceSafety[S, F]): S[F, O] =

    rs.bracketSink(newByteChannelRW(p, openOptions))(_.close)(run)


  private[this] def newByteChannelRW(p: P, oos: Seq[OpenRWOption]) =
    tw(
      new JVMSeekableByteChannelIO(
        Files.newByteChannel(
          p,
          StandardOpenOption.READ +: StandardOpenOption.WRITE +: oos.map { _.jOO }: _*
        ),
        tw
      )
    )
}

class JVMFileReadIO[F[_]](tw: ThunkWrap[F]) extends FileReadIO[F, JVMFileIO.Module[F]] {
  type P = JVMFSIO.Module[F]#P

  def readAllBytes(p: P): F[Array[Byte]] = tw(Files.readAllBytes(p))
  def readAllLines(p: P, cs: JVMRequiredCharsetIO[F]): F[JListFoldable[String]] = ???

  def onInputStreamF[X](p: P)(run: InputStreamIO[F] => F[X])(implicit brkt: Bracket[F]): F[X] =
    brkt.bracket(newInputStream(p))(_.close)(run)

  def onInputStreamS[S[_[_], _], X](p: P)
                                   (s: InputStreamIO[F] => S[F,X])
                                   (implicit rs: ResourceSafety[S,F]): S[F,X] =
    rs.bracketSource(newInputStream(p))(_.close)(s)

  private[this] def newInputStream(p: P) =
    tw(new JVMInputStreamIO(Files.newInputStream(p), tw))


  type SBCReadIO = SeekableByteChannelReadIO[F, JVMByteBufferModule[F]]

  def onByteChannelROF[X](p: P, openOptions: OpenReadOption*)
                         (run: SBCReadIO => F[X])
                         (implicit brkt: Bracket[F]): F[X] =
    brkt.bracket(newByteChannelRO(p, openOptions))(_.close)(run)

  def onByteChannelROS[S[_[_], _], X](p: P, openOptions: OpenReadOption*)
                                     (run: SBCReadIO => S[F, X])
                                     (implicit rs: ResourceSafety[S, F]): S[F, X] =
    rs.bracketSource(newByteChannelRO(p, openOptions))(_.close)(run)

  private[this] def newByteChannelRO(p: P, oos: Seq[OpenReadOption]) =
    tw(
      new JVMSeekableByteChannelReadIO(
        Files.newByteChannel(
          p,
          StandardOpenOption.READ +: oos.map { _.jOO }: _*
        ),
        tw
      )
    )
}

trait JVMFileWriteIOImpl[F[_]] extends FileWriteIO[F, JVMFileIO.Module[F]] {
  type P = JVMFSIO.Module[F]#P

  def tw: ThunkWrap[F]

  def writeByteArray(f: P, content: Array[Byte]): F[P] =
    tw(
      JVMFSPathLogic.tagCheck(f, Files.createFile(f))
    )


  def onByteChannelAppendF[X](p: P, openOptions: OpenAppendOption*)
                             (run: ByteChannelWriteIO[F, JVMByteBufferModule[F]] => F[X])
                             (implicit brkt: Bracket[F]): F[X] =
    brkt.bracket(newByteChannelAppend(p, openOptions))(_.close)(run)

  def onByteChannelAppendS[S[_[_], _], I, O]
                          (p: P, openOptions: OpenAppendOption*)
                          (run: ByteChannelWriteIO[F, JVMByteBufferModule[F]] => S[F, I] => S[F, O])
                          (implicit rs: ResourceSafety[S, F]): S[F, O] =
    rs.bracketSink(newByteChannelAppend(p, openOptions))(_.close)(run)

  private[this] def newByteChannelAppend(p: P, oos: Seq[OpenAppendOption]) =
    tw(
      new JVMByteChannelWriteIO(
        Files.newByteChannel(
          p,
          StandardOpenOption.APPEND +: oos.map { _.jOO }: _*
        ),
        tw
      )
    )
}

class JVMFileWriteIO[F[_]](val tw: ThunkWrap[F]) extends JVMFileWriteIOImpl[F]
