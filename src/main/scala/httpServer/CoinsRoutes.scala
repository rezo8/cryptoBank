package httpServer

import httpServer.Helpers.handleRepositoryProcess
import httpServer.Requests.{AddCoinToWalletRequest, UpdateCoinAmountRequest}
import models.{UserWithCoins, WalletCoin}
import repository.CoinsRepository
import zio.*
import zio.http.*
import zio.json.*

import java.util.UUID
import scala.concurrent.ExecutionContext

abstract class CoinsRoutes extends RouteContainer {
  val coinsRepository: CoinsRepository
  implicit val ec: ExecutionContext
  private val rootUrl = "coins"

  override val routes: Routes[Any, Response] = Routes(
    Method.POST / rootUrl -> handler { handleAddCoinToWallet(_) },
    Method.PUT / rootUrl -> handler { (req: Request) =>
      handleUpdateCoinValue(req)
    },
    Method.GET / rootUrl / "coinUuid" / zio.http.uuid("coinUuid") -> handler {
      (id: UUID, _: Request) => handleLoadCoinByUserId(id)
    },
    Method.GET / rootUrl / "userId" / zio.http.uuid("userId") -> handler {
      (id: UUID, _: Request) => handleLoadCoinByUserId(id)
    }
  )

  private def handleLoadCoinByUserId(id: UUID): ZIO[Any, Nothing, Response] = {
    handleRepositoryProcess[UserWithCoins](for {
      loadRes <- coinsRepository.loadCoinsForUser(id)
    } yield Right(loadRes))
  }

  private def handleLoadCoinsByWalletId(
      id: UUID
  ): ZIO[Any, Nothing, Response] = {
    handleRepositoryProcess[List[WalletCoin]](for {
      loadRes <- coinsRepository.loadCoinsForWallet(id)
    } yield Right(loadRes))
  }

  private def handleUpdateCoinValue(
      req: Request
  ): ZIO[Any, Nothing, Response] = {
    handleRepositoryProcess[String](for {
      requestString <- req.body.asString
      updateCoinAmount <- ZIO.fromEither(
        requestString.fromJson[UpdateCoinAmountRequest]
      )
      updateRes <- coinsRepository.updateCoinOwnedAmount(
        updateCoinAmount.coinId,
        updateCoinAmount.amount
      )
    } yield Right("successful update"))
  }

  private def handleAddCoinToWallet(
      req: Request
  ): ZIO[Any, Nothing, Response] = {
    handleRepositoryProcess[String](for {
      userBodyString <- req.body.asString
      addCoinToWallet <- ZIO.fromEither(
        userBodyString.fromJson[AddCoinToWalletRequest]
      )
      createRes <- coinsRepository
        .addCoinToWallet(
          addCoinToWallet.coinId,
          addCoinToWallet.coinName,
          addCoinToWallet.walletId,
          addCoinToWallet.amount
        )
        .map(_ => "success")
    } yield Right(createRes))
  }
}
