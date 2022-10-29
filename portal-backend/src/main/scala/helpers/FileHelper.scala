package helpers

import zio.ZIO

import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object FileHelper {

    def makeTempFile(prefix: String, suffix: String): ZIO[Any, Throwable, File] =
        ZIO.from(File.createTempFile(prefix, suffix))

    def makeFile(path: String): ZIO[Any, Throwable, File] = for {
        newFile <- ZIO.from(new File(path: String))
        _ <- ZIO.from(newFile.getParentFile.mkdirs)
        _ <- ZIO.from(newFile.createNewFile())
    } yield newFile

}
