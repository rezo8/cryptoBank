package httpServer

import httpServer.Helpers.handleRepositoryProcess
import httpServer.Requests.CreateAccountRequest
import httpServer.Responses.{CreateAccountResponse, LoadAccountResponse}
import models.Account
import repository.AccountsRepository
import zio.*
import zio.http.*
import zio.json.*

import java.util
import java.util.UUID
import scala.concurrent.ExecutionContext

abstract class AccountsRoutes extends RouteContainer {
  val accountsRepository: AccountsRepository
  implicit val ec: ExecutionContext
  private val rootUrl = "accounts"

  override val routes: Routes[Any, Response] = Routes(
    Method.POST / rootUrl -> handler { handleCreateAccount(_) },
    Method.GET / rootUrl / "userId" / zio.http.uuid("userId") -> handler {
      (id: UUID, _: Request) => handleLoadByUserId(id)
    }
  )

  private def handleLoadByUserId(id: UUID): ZIO[Any, Nothing, Response] = {
    handleRepositoryProcess[LoadAccountResponse](for {
      loadRes <- accountsRepository.getAccountsByUserId(id)
    } yield loadRes.map(LoadAccountResponse(_)))
  }

  private def handleCreateAccount(
      req: Request
  ): ZIO[Any, Nothing, Response] = {
    handleRepositoryProcess[CreateAccountResponse](for {
      userBodyString <- req.body.asString
      createAccountRequest <- ZIO.fromEither(
        userBodyString.fromJson[CreateAccountRequest]
      )
      createRes <- accountsRepository
        .safeCreateAccount(
          createAccountRequest.userId,
          createAccountRequest.cryptoType,
          createAccountRequest.accountName
        )
    } yield createRes.map(accountId => CreateAccountResponse(accountId)))
  }
}
