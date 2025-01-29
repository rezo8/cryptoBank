package httpServer

import httpServer.Helpers.handleServerResponse
import httpServer.Requests.CreateAccountRequest
import httpServer.Responses.{CreateAccountResponse, LoadAccountResponse}
import services.AccountsService
import services.Exceptions.{Unexpected, UnparseableRequest}
import zio.*
import zio.http.*
import zio.json.*

import java.util
import java.util.UUID
import scala.concurrent.ExecutionContext

abstract class AccountsRoutes extends RouteContainer {
  val accountsService: AccountsService
  implicit val ec: ExecutionContext
  private val rootUrl = "accounts"

  override val routes: Routes[Any, Response] = Routes(
    Method.POST / rootUrl -> handler { handleCreateAccount(_) },
    Method.GET / rootUrl / "userId" / zio.http.uuid("userId") -> handler {
      (id: UUID, _: Request) => handleLoadByUserId(id)
    }
  )

  private def handleLoadByUserId(id: UUID): ZIO[Any, Nothing, Response] = {
    handleServerResponse[LoadAccountResponse](for {
      loadRes <- accountsService.getAccountsByUserId(id)
    } yield LoadAccountResponse(loadRes))
  }

  private def handleCreateAccount(
      req: Request
  ): ZIO[Any, Nothing, Response] = {
    handleServerResponse[CreateAccountResponse](for {
      userBodyString <- req.body.asString.mapError(Unexpected(_))
      createAccountRequest <- ZIO.fromEither(
        userBodyString
          .fromJson[CreateAccountRequest]
          .left
          .map(UnparseableRequest(_))
      )
      accountId <- accountsService.createAccount(
        createAccountRequest.userId,
        createAccountRequest.cryptoType,
        createAccountRequest.accountName
      )
    } yield CreateAccountResponse(accountId))
  }
}
