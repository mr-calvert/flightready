package flightready.java.nio.file

import scala.reflect.ClassTag
import flightready.integration.category.FlatMap
import flightready.integration.effect.ThunkWrap
import flightready.java.nio.{BufferIO, BufferReadIO}

class PutBufferFallback[F[_], A: ClassTag](bufIO: BufferIO[F, A], tw: ThunkWrap[F], fm: FlatMap[F]) {
  def putBufferViaBackingArray(rwBuf: BufferReadIO[F, A] with BufferIO[F, A]): F[Unit] =
    fm.flatMap(rwBuf.hasArray) {
      if (_)
        fm.flatMap(rwBuf.remaining) { remaining =>
          if (remaining <= 0) tw.apply(())
          else fm.flatMap(rwBuf.array) { array => fm.flatMap(rwBuf.arrayOffset) { bufIO.putArraySlice(array, _, remaining) } }
        }
      else putBufferViaTempCopy(rwBuf)
    }

  def putBufferViaTempCopy(in: BufferReadIO[F, A]): F[Unit] =
    fm.flatMap(in.remaining) { remaining =>
      if (remaining < 0)
        tw(())
      else {
        val tmp = new Array[A](remaining)
        fm.flatMap(in.getInto(tmp)) { _ => bufIO.putArray(tmp) }
      }
    }
}

