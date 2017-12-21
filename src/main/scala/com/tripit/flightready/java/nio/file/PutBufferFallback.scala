package com.tripit.flightready.java.nio.file

import scala.language.higherKinds
import scala.reflect.ClassTag
import com.tripit.flightready.integration.category.FlatMap
import com.tripit.flightready.integration.effect.ThunkWrap
import com.tripit.flightready.java.nio.{BufferIO, BufferReadIO}

class PutBufferFallback[F[_], A: ClassTag](bufIO: BufferIO[F, A], tw: ThunkWrap[F], fm: FlatMap[F]) {
  def putBufferViaBackingArray(rwBuf: BufferReadIO[F, A] with BufferIO[F, A]): F[Unit] =
    if(rwBuf.hasArray)
      fm.flatMap(rwBuf.remaining) { remaining =>
        if (remaining <= 0) tw.wrap(())
        else bufIO.putArraySlice(rwBuf.array, rwBuf.arrayOffset, remaining)
      }
    else putBufferViaTempCopy(rwBuf)

  def putBufferViaTempCopy(in: BufferReadIO[F, A]): F[Unit] =
    fm.flatMap(in.remaining) { remaining =>
      if (remaining < 0)
        tw.wrap(())
      else {
        val tmp = new Array[A](remaining)
        fm.flatMap(in.getInto(tmp)) { _ => bufIO.putArray(tmp) }
      }
    }
}

