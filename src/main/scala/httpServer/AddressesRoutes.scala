package httpServer

import httpServer.Helpers.handleRepositoryProcess
import httpServer.Requests.{
  AddCoinToAccountRequest,
  CreateAddressRequest,
  UpdateCoinAmountRequest
}
import httpServer.Responses.*
import models.{AccountCoin, BitcoinAddressValue, CoinValue, UserWithCoins}
import repository.{AddressRepository, CoinsRepository}
import zio.*
import zio.http.*
import zio.json.*

import java.util.UUID
import scala.concurrent.ExecutionContext

abstract class AddressesRoutes extends RouteContainer {
  val addressRepository: AddressRepository
  implicit val ec: ExecutionContext
  private val rootUrl = "addresses"

  override val routes: Routes[Any, Response] = Routes(
    Method.POST / rootUrl -> handler { handleCreateAddress(_) },
    Method.POST / rootUrl / "addressId" / zio.http.uuid("addressId")
      -> handler { (addressId: UUID, req: Request) =>
        handleUpdateAddressValue(addressId, req)
      },
    Method.GET / rootUrl / "accountId" / zio.http.uuid("accountId") -> handler {
      (id: UUID, _: Request) => handleLoadAddressesByAccountId(id)
    }
  )

  private def handleCreateAddress(req: Request): ZIO[Any, Nothing, Response] = {
    // TODO fix repo collisions
    handleRepositoryProcess[CreateAddressResponse](
      for {
        requestString <- req.body.asString
        createCoinRequest <- ZIO.fromEither(
          requestString.fromJson[CreateAddressRequest]
        )
        addressId <- addressRepository.createBitcoinAddress(
          createCoinRequest.accountId,
          createCoinRequest.addressLocation,
          BitcoinAddressValue(createCoinRequest.balance) // TODO handle invalid
        )
      } yield Right(
        CreateAddressResponse(addressId)
      )
    )
  }

  private def handleLoadAddressesByAccountId(
      id: UUID
  ): ZIO[Any, Nothing, Response] = {
    handleRepositoryProcess[LoadAddressesForAccountResponse](for {
      loadRes <- addressRepository.getAddressesByAccountId(id)
      addresses <- ZIO.fromEither(loadRes)
    } yield Right(LoadAddressesForAccountResponse(addresses)))
  }

  private def handleUpdateAddressValue(
      addressId: UUID,
      req: Request
  ): ZIO[Any, Nothing, Response] = {
    handleRepositoryProcess[MessageResponse](for {
      requestString <- req.body.asString
      updateCoinAmount <- ZIO.fromEither(
        requestString.fromJson[UpdateCoinAmountRequest]
      )
      updateRes <- addressRepository.updateBitcoinAddressValue(
        addressId,
        BitcoinAddressValue(updateCoinAmount.satoshis)
      )
    } yield Right(MessageResponse("successful update")))
  }

}
