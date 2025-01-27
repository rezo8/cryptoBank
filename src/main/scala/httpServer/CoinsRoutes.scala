package httpServer

import httpServer.Helpers.handleRepositoryProcess
import httpServer.Requests.{
  AddCoinToWalletRequest,
  CreateCoinRequest,
  UpdateCoinAmountRequest
}
import httpServer.Responses.{
  AddCoinToWalletResponse,
  CreateCoinResponse,
  LoadUserWithCoinsResponse,
  MessageResponse,
  WalletCoinsResponse
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
    Method.PUT / rootUrl / "walletCoinId" / zio.http.uuid(
      "walletCoinId"
    ) -> handler { (walletCoinId: UUID, req: Request) =>
      handleUpdateCoinValue(walletCoinId, req)
    }
  )

  private def handleCreateCoin(req: Request): ZIO[Any, Nothing, Response] = {
    // TODO fix repo collisions
    handleRepositoryProcess[CreateCoinResponse](
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
        CreateCoinResponse(createCoinRequest.coinId, createCoinRequest.coinName)
      )
    )
  }

  private def handleLoadCoinByUserId(id: UUID): ZIO[Any, Nothing, Response] = {
    handleRepositoryProcess[LoadUserWithCoinsResponse](for {
      loadRes <- coinsRepository
        .loadCoinsForUser(id)
        .map(userWithCoins => {
          LoadUserWithCoinsResponse(
            userId = userWithCoins.userId,
            walletCoins = userWithCoins.walletCoins
          )
        })
    } yield Right(loadRes))
  }

  private def handleLoadCoinsByWalletId(
      id: UUID
  ): ZIO[Any, Nothing, Response] = {
    handleRepositoryProcess[WalletCoinsResponse](for {
      loadRes <- coinsRepository
        .loadCoinsForWallet(id)
        .map(WalletCoinsResponse(_))
    } yield Right(loadRes))
  }

  private def handleUpdateCoinValue(
      walletCoinId: UUID,
      req: Request
  ): ZIO[Any, Nothing, Response] = {
    handleRepositoryProcess[MessageResponse](for {
      requestString <- req.body.asString
      updateCoinAmount <- ZIO.fromEither(
        requestString.fromJson[UpdateCoinAmountRequest]
      )
      updateRes <- coinsRepository.updateWalletCoinOwnedSatoshi(
        walletCoinId,
        CoinValue(updateCoinAmount.satoshis)
      )
    } yield Right(MessageResponse("successful update")))
  }

  private def handleAddCoinToWallet(
      coinId: UUID,
      walletId: UUID,
      req: Request
  ): ZIO[Any, Nothing, Response] = {
    handleRepositoryProcess[AddCoinToWalletResponse](for {
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
        .map(walletCoinId => AddCoinToWalletResponse(walletCoinId))
    } yield Right(createRes))
  }
}
