package flightready.java.nio.file

import java.nio.file.{StandardOpenOption, Files}

import flightready.integration.effect.{ThunkWrap, Bracket}
import flightready.integration.streaming.ResourceSafety
import flightready.java.JVMA
import flightready.java.io.{JVMInputStreamIO, InputStreamIO}
import flightready.java.nio._

object JVMFileIO {
  class Module[F[_]] extends FileIO.Module[F] {
    def fileIO: FileIO[F, JVMFileIO.Module[F]] = ???
  }
}

class JVMFileIO[F[_]](val tw: ThunkWrap[F]) 
      extends JVMFileReadIO[F](tw) with FileIO[F, JVMA] with JVMFileWriteIOImpl[F] {

  import JVMFSPath.asPath
  override type P[D] = FSPath.P[JVMA, D]

  def onByteChannelRWF[X](p: P[_], openOptions: OpenRWOption*)
                         (run: SeekableByteChannelIO[F, JVMA] => F[X])
                         (implicit brkt: Bracket[F]): F[X] =

    brkt.bracket(newByteChannelRW(p, openOptions))(_.close)(run)


  def onByteChannelRWS[S[_[_], _], I, O]
                      (p: P[_], openOptions: OpenRWOption*)
                      (run: SeekableByteChannelIO[F, JVMA] => S[F, I] => S[F, O])
                      (implicit rs: ResourceSafety[S, F]): S[F, O] =

    rs.bracketSink(newByteChannelRW(p, openOptions))(_.close)(run)


  private[this] def newByteChannelRW(p: P[_], oos: Seq[OpenRWOption]): F[SeekableByteChannelIO[F, JVMA]] =
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

class JVMFileReadIO[F[_]](tw: ThunkWrap[F]) extends FileReadIO[F, JVMA] {
  import JVMFSPath.asPath

  def readAllBytes(p: P[_]): F[Array[Byte]] = tw(Files.readAllBytes(p))

  def onInputStreamF[X](p: P[_])(run: InputStreamIO[F, JVMA] => F[X])(implicit brkt: Bracket[F]): F[X] =
    brkt.bracket(newInputStream(p))(_.close)(run)

  def onInputStreamS[S[_[_], _], X](p: P[_])
                                   (s: InputStreamIO[F, JVMA] => S[F,X])
                                   (implicit rs: ResourceSafety[S,F]): S[F,X] =
    rs.bracketSource(newInputStream(p))(_.close)(s)

  private[this] def newInputStream(p: P[_]): F[InputStreamIO[F, JVMA]] =
    tw(new JVMInputStreamIO(Files.newInputStream(p), tw))


  def onByteChannelROF[X](p: P[_], openOptions: OpenReadOption*)
                         (run: SeekableByteChannelReadIO[F, JVMA] => F[X])
                         (implicit brkt: Bracket[F]): F[X] =

    brkt.bracket(newByteChannelRO(p, openOptions))(_.close)(run)


  def onByteChannelROS[S[_[_], _], X](p: P[_], openOptions: OpenReadOption*)
                                     (run: SeekableByteChannelReadIO[F, JVMA] => S[F, X])
                                     (implicit rs: ResourceSafety[S, F]): S[F, X] =

    rs.bracketSource(newByteChannelRO(p, openOptions))(_.close)(run)


  private[this] def newByteChannelRO(p: P[_], oos: Seq[OpenReadOption]): F[SeekableByteChannelReadIO[F, JVMA]] =
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

trait JVMFileWriteIOImpl[F[_]] extends FileWriteIO[F, JVMA] {
  import JVMFSPath.asPath

  def tw: ThunkWrap[F]

  def writeByteArray[D](f: P[D], content: Array[Byte]): F[P[D]] =
    tw(JVMFSPath.tagCheck(f, Files.createFile(f)))


  def onByteChannelAppendF[X](p: P[_], openOptions: OpenAppendOption*)
                             (run: ByteChannelWriteIO[F, JVMA] => F[X])
                             (implicit brkt: Bracket[F]): F[X] =

    brkt.bracket(newByteChannelAppend(p, openOptions))(_.close)(run)


  def onByteChannelAppendS[S[_[_], _], I, O]
                          (p: P[_], openOptions: OpenAppendOption*)
                          (run: ByteChannelWriteIO[F, JVMA] => S[F, I] => S[F, O])
                          (implicit rs: ResourceSafety[S, F]): S[F, O] =

    rs.bracketSink(newByteChannelAppend(p, openOptions))(_.close)(run)


  private[this] def newByteChannelAppend(p: P[_], oos: Seq[OpenAppendOption]): F[ByteChannelWriteIO[F, JVMA]] =
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
