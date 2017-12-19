package com.tripit.flightready.java.nio

import scala.language.higherKinds

trait ChannelReadIO[F[_], ByteBufferMod <: ByteBufferIO.Module[F]] {
  def read(bbioOut: ByteBufferMod#IORW): F[Int]
}

trait ChannelIO[F[_], ByteBufferMod <: ByteBufferIO.Module[F]] extends ChannelReadIO[F, ByteBufferMod] {
  def write(bbioIn: ByteBufferMod#IORO): F[Int]
}

trait SeekableChannelReadIO[F[_], ByteBufferMod <: ByteBufferIO.Module[F]] extends ChannelReadIO[F, ByteBufferMod] {
  def position: F[Long]
  def setPosition(pos: Long): F[Unit]

  def size: F[Long]

  def truncate(size: Long): F[Unit]
}

trait SeekableChannelIO[F[_], ByteBufferMod <: ByteBufferIO.Module[F]]
  extends SeekableChannelReadIO[F, ByteBufferMod] with ChannelIO[F, ByteBufferMod]
