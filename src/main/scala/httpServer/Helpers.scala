package httpServer

import repository.Exceptions.ServerException
import zio.*
import zio.http.*
import zio.json.*

object Helpers {
  def handleRepositoryProcess[A](
      repoProc: ZIO[Any, Serializable, Either[ServerException, A]]
  )(implicit enc: zio.json.JsonEncoder[A]): ZIO[Any, Nothing, Response] = {
    repoProc.fold(
      err => {
        println(err) // TODO add logging
        Response.internalServerError("unexpected error")
      },
      success => {
        success.fold(
          error =>
            Response.error(status = error.status, message = error.getMessage),
          success => {
            Response.text(success.toJson)
          }
        )
      }
    )
  }
}
