package com.tripit.flightready.java.nio

import scala.language.higherKinds

trait ByteChannelReadIO[F[_], ByteBufferMod <: ByteBufferIO.Module[F]] {
  def read(bbioOut: ByteBufferMod#IORW): F[Int]
}

trait ByteChannelIO[F[_], ByteBufferMod <: ByteBufferIO.Module[F]] extends ByteChannelReadIO[F, ByteBufferMod] {
  def write(bbioIn: ByteBufferMod#IORO): F[Int]
}

trait SeekableByteChannelReadIO[F[_], ByteBufferMod <: ByteBufferIO.Module[F]]
      extends ByteChannelReadIO[F, ByteBufferMod] {

  def position: F[Long]
  def setPosition(pos: Long): F[Unit]

  def size: F[Long]

  def truncate(size: Long): F[Unit]
}

trait SeekableByteByteChannelIO[F[_], ByteBufferMod <: ByteBufferIO.Module[F]]
  extends SeekableByteChannelReadIO[F, ByteBufferMod] with ByteChannelIO[F, ByteBufferMod]
