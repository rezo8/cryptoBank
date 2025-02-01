package httpServer

import httpServer.Requests.CreateAccountRequest
import httpServer.Responses.{CreateAccountResponse, LoadAccountResponse}
import services.AccountsService
import zio.*
import zio.http.*

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
    handleServerResponseWithRequest[
      CreateAccountRequest,
      CreateAccountResponse
    ](
      req,
      (createAccountRequest: CreateAccountRequest) => {
        accountsService
          .createAccount(
            createAccountRequest.userId,
            createAccountRequest.cryptoType,
            createAccountRequest.accountName
          )
          .map(CreateAccountResponse(_))
      }
    )
  }
}
