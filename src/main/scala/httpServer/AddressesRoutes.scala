package httpServer

import httpServer.Helpers.handleRepositoryProcess
import httpServer.Requests.{CreateAddressRequest, UpdateAddressAmountRequest}
import httpServer.Responses.*
import models.BitcoinAddressValue
import repository.AddressRepository
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
    Method.PUT / rootUrl / "addressId" / zio.http.uuid("addressId")
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
        createAddressRequest <- ZIO.fromEither(
          requestString.fromJson[CreateAddressRequest]
        )
        addressId <- addressRepository.createBitcoinAddress(
          createAddressRequest.accountId,
          createAddressRequest.addressLocation,
          BitcoinAddressValue(
            createAddressRequest.balance
          ) // TODO handle invalid
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
      updateAddressAmount <- ZIO.fromEither(
        requestString.fromJson[UpdateAddressAmountRequest]
      )
      updateRes <- addressRepository.updateBitcoinAddressValue(
        addressId,
        BitcoinAddressValue(updateAddressAmount.satoshis)
      )
    } yield Right(MessageResponse("successful update")))
  }

}
