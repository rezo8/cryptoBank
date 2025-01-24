package httpServer

import httpServer.Helpers.handleRepositoryProcess
import httpServer.Requests.{
  AddCoinToWalletRequest,
  CreateCoinRequest,
  UpdateCoinAmountRequest
}
import models.{CoinValue, UserWithCoins, WalletCoin}
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
    Method.POST / rootUrl -> handler { handleCreateCoin(_) },
    Method.GET / rootUrl / "userId" / zio.http.uuid("userId") -> handler {
      (id: UUID, _: Request) => handleLoadCoinByUserId(id)
    },
    Method.POST / rootUrl / "coinId" / zio.http.uuid(
      "coinId"
    ) / "walletId" / zio.http.uuid("walletId") -> handler {
      (coinId: UUID, walletId: UUID, req: Request) =>
        handleAddCoinToWallet(coinId, walletId, req)
    },
    Method.PUT / rootUrl / "walletCoinId" / zio.http.int(
      "walletCoinId"
    ) -> handler { (walletCoinId: Int, req: Request) =>
      handleUpdateCoinValue(walletCoinId, req)
    }
  )

  private def handleCreateCoin(req: Request): ZIO[Any, Nothing, Response] = {
    // TODO fix repo collisions
    handleRepositoryProcess[String](
      for {
        requestString <- req.body.asString
        createCoinRequest <- ZIO.fromEither(
          requestString.fromJson[CreateCoinRequest]
        )
        updateRes <- coinsRepository.createCoin(
          createCoinRequest.coinId,
          createCoinRequest.coinName
        )
      } yield Right(
        s"successfully created coin with id [${createCoinRequest.coinId}]"
      )
    )
  }

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
      walletCoinId: Int,
      req: Request
  ): ZIO[Any, Nothing, Response] = {
    handleRepositoryProcess[String](for {
      requestString <- req.body.asString
      updateCoinAmount <- ZIO.fromEither(
        requestString.fromJson[UpdateCoinAmountRequest]
      )
      updateRes <- coinsRepository.updateCoinOwnedSatoshi(
        walletCoinId,
        CoinValue(updateCoinAmount.satoshis)
      )
    } yield Right("successful update"))
  }

  private def handleAddCoinToWallet(
      coinId: UUID,
      walletId: UUID,
      req: Request
  ): ZIO[Any, Nothing, Response] = {
    handleRepositoryProcess[String](for {
      userBodyString <- req.body.asString
      addCoinToWallet <- ZIO.fromEither(
        userBodyString.fromJson[AddCoinToWalletRequest]
      )
      createRes <- coinsRepository
        .addCoinToWallet(
          coinId,
          walletId,
          CoinValue(addCoinToWallet.satoshis)
        )
        .map(_ => "success")
    } yield Right(createRes))
  }
}
