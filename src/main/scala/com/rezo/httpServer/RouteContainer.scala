package com.rezo.httpServer

import Requests.ServerRequest
import Responses.ServerResponse
import com.rezo.services.Exceptions.{ServerException, Unexpected, UnparseableRequest}
import com.rezo.utils.ZioTypes.RezoTask
import zio.*
import zio.http.*
import zio.json.*

trait RouteContainer {
  def routes: Routes[Any, Response]

  def parseRequest[A <: ServerRequest](req: Request)(implicit
      dec: zio.json.JsonDecoder[A]
  ): ZIO[Any, ServerException, A] = {
    for {
      reqBody <- req.body.asString.mapError(Unexpected(_))
      request <- ZIO.fromEither(
        reqBody
          .fromJson[A]
          .left
          .map(UnparseableRequest(_))
      )
    } yield request
  }

  def handleServerResponseWithRequest[
      A <: ServerRequest,
      B <: ServerResponse
  ](req: Request, serverProc: A => RezoTask[B])(implicit
      dec: zio.json.JsonDecoder[A],
      enc: zio.json.JsonEncoder[B]
  ): ZIO[Any, Nothing, Response] = {
    handleServerResponse(parseRequest(req).flatMap(serverProc(_)))
  }

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
