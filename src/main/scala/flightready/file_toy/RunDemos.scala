package flightready.file_toy

import java.nio.file.Paths

import cats.effect.IO

object RunDemos {
  import JVMFileSystem.module

  def main(args: Array[String]): Unit = {
    Demos.helloWorld[IO](Paths.get("~/hello.txt")).unsafeRunSync()
  }
}
