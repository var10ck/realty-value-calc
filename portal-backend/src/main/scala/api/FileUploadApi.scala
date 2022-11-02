package api

import helpers.{ExcelHelper, FileHelper}
import zhttp.http._
import zio.ZIO
import zio.json.EncoderOps
import zio.stream.ZSink

import java.io.FileInputStream

object FileUploadApi {

    val api = Http.collectZIO[Request] {
        case req @ Method.PUT -> !! / "upload" / "file" =>
            (for {
                tempFile <- FileHelper.makeTempFileZIO("upload", ".xls")
                result <- req.body.asStream.run(ZSink.fromFile(tempFile))
            } yield (tempFile, result)).fold(
              e => {
                  e.printStackTrace()
                  Response.status(Status.InternalServerError)
              },
              result => Response.text(s"created file ${result._1.getAbsolutePath}: ${result._2} bytes written")
            )

    } @@ Middleware.debug

}
