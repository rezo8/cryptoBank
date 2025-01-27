package httpServer

import httpServer.Helpers.handleRepositoryProcess
import httpServer.Requests.{
  AddCoinToAccountRequest,
  CreateCoinRequest,
  UpdateCoinAmountRequest
}
import httpServer.Responses.*
import models.{AccountCoin, CoinValue, UserWithCoins}
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
    ) / "accountId" / zio.http.uuid("accountId") -> handler {
      (coinId: UUID, accountId: UUID, req: Request) =>
        handleAddCoinToAccount(coinId, accountId, req)
    },
    Method.PUT / rootUrl / "accountCoinId" / zio.http.uuid(
      "accountCoinId"
    ) -> handler { (accountCoinId: UUID, req: Request) =>
      handleUpdateCoinValue(accountCoinId, req)
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
            accountCoins = userWithCoins.accountCoins
          )
        })
    } yield Right(loadRes))
  }

  private def handleLoadCoinsByAccountId(
      id: UUID
  ): ZIO[Any, Nothing, Response] = {
    handleRepositoryProcess[AccountCoinsResponse](for {
      loadRes <- coinsRepository
        .loadCoinsForAccount(id)
        .map(AccountCoinsResponse(_))
    } yield Right(loadRes))
  }

  private def handleUpdateCoinValue(
      accountCoinId: UUID,
      req: Request
  ): ZIO[Any, Nothing, Response] = {
    handleRepositoryProcess[MessageResponse](for {
      requestString <- req.body.asString
      updateCoinAmount <- ZIO.fromEither(
        requestString.fromJson[UpdateCoinAmountRequest]
      )
      updateRes <- coinsRepository.updateAccountCoinOwnedSatoshi(
        accountCoinId,
        CoinValue(updateCoinAmount.satoshis)
      )
    } yield Right(MessageResponse("successful update")))
  }

  private def handleAddCoinToAccount(
      coinId: UUID,
      accountId: UUID,
      req: Request
  ): ZIO[Any, Nothing, Response] = {
    handleRepositoryProcess[AddCoinToAccountResponse](for {
      userBodyString <- req.body.asString
      addCoinToAccount <- ZIO.fromEither(
        userBodyString.fromJson[AddCoinToAccountRequest]
      )
      createRes <- coinsRepository
        .addCoinToAccount(
          coinId,
          accountId,
          CoinValue(addCoinToAccount.satoshis)
        )
        .map(accountCoinId => AddCoinToAccountResponse(accountCoinId))
    } yield Right(createRes))
  }
}
