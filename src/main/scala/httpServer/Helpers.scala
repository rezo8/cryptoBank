package httpServer

import httpServer.Responses.ServerResponse
import repository.Exceptions.ServerException
import utils.ZioTypes.RezoTask
import zio.*
import zio.http.*
import zio.json.*

object Helpers {

  // TODO add req parsing.
  def handleServerResponse[A <: ServerResponse](
      serverProc: RezoTask[A]
  )(implicit enc: zio.json.JsonEncoder[A]): ZIO[Any, Nothing, Response] = {
    serverProc.fold(
      error =>
        Response.error(status = error.status, message = error.getMessage),
      success => Response.text(success.toJson)
    )
  }
}
