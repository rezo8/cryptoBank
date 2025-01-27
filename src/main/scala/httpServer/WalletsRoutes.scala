package httpServer

import httpServer.Helpers.handleRepositoryProcess
import httpServer.Requests.CreateWalletRequest
import httpServer.Responses.{CreateWalletResponse, LoadWalletResponse}
import models.Wallet
import repository.WalletsRepository
import zio.*
import zio.http.*
import zio.json.*

import java.util
import java.util.UUID
import scala.concurrent.ExecutionContext

abstract class WalletsRoutes extends RouteContainer {
  val walletsRepository: WalletsRepository
  implicit val ec: ExecutionContext
  private val rootUrl = "wallets"

  override val routes: Routes[Any, Response] = Routes(
    Method.POST / rootUrl -> handler { handleCreateWallet(_) },
    Method.GET / rootUrl / "userId" / zio.http.uuid("userId") -> handler {
      (id: UUID, _: Request) => handleLoadByUserId(id)
    }
  )

  private def handleLoadByUserId(id: UUID): ZIO[Any, Nothing, Response] = {
    handleRepositoryProcess[LoadWalletResponse](for {
      loadRes <- walletsRepository.getWalletsByUserId(id)
    } yield loadRes.map(LoadWalletResponse(_)))
  }

  private def handleCreateWallet(
      req: Request
  ): ZIO[Any, Nothing, Response] = {
    handleRepositoryProcess[CreateWalletResponse](for {
      userBodyString <- req.body.asString
      createWalletRequest <- ZIO.fromEither(
        userBodyString.fromJson[CreateWalletRequest]
      )
      createRes <- walletsRepository
        .safeCreateWallet(
          createWalletRequest.userId,
          createWalletRequest.currency,
          createWalletRequest.walletName
        )
    } yield createRes.map(walletId => CreateWalletResponse(walletId)))
  }

}
