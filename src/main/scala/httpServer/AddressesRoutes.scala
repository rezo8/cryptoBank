package httpServer

import httpServer.Helpers.handleServerResponse
import httpServer.Requests.{CreateAddressRequest, UpdateAddressAmountRequest}
import httpServer.Responses.*
import models.BitcoinAddressValue
import services.AddressesService
import services.Exceptions.{Unexpected, UnparseableRequest}
import zio.*
import zio.http.*
import zio.json.*

import java.util.UUID
import scala.concurrent.ExecutionContext

abstract class AddressesRoutes extends RouteContainer {
  val addressesService: AddressesService
  implicit val ec: ExecutionContext
  private val rootUrl = "addresses"

  override val routes: Routes[Any, Response] = Routes(
    Method.POST / rootUrl -> handler { handleCreateAddress(_) },
    Method.PUT / rootUrl / "accountId" / zio.http.uuid("accountId")
      -> handler { (addressId: UUID, req: Request) =>
        handleUpdateAddressValue(addressId, req)
      },
    Method.GET / rootUrl / "accountId" / zio.http.uuid("accountId") -> handler {
      (id: UUID, _: Request) => handleLoadAddressesByAccountId(id)
    }
  )

  private def handleCreateAddress(req: Request): ZIO[Any, Nothing, Response] = {
    // TODO fix repo collisions
    handleServerResponse[CreateAddressResponse](
      for {
        requestString <- req.body.asString.mapError(Unexpected(_))
        createAddressRequest <- ZIO.fromEither(
          requestString
            .fromJson[CreateAddressRequest]
            .left
            .map(x => UnparseableRequest(x))
        )
        addressId <- addressesService.createBitcoinAddress(
          createAddressRequest.accountId,
          createAddressRequest.addressLocation,
          BitcoinAddressValue(
            createAddressRequest.balance
          )
        )
      } yield CreateAddressResponse(addressId)
    )
  }

  private def handleLoadAddressesByAccountId(
      id: UUID
  ): ZIO[Any, Nothing, Response] = {
    handleServerResponse[LoadAddressesForAccountResponse](for {
      addresses <- addressesService.getAddressesByAccountId(id)
    } yield LoadAddressesForAccountResponse(addresses))
  }

  private def handleUpdateAddressValue(
      addressId: UUID,
      req: Request
  ): ZIO[Any, Nothing, Response] = {
    handleServerResponse[MessageResponse](for {
      requestString <- req.body.asString.mapError(Unexpected(_))
      updateAddressAmount <- ZIO.fromEither(
        requestString
          .fromJson[UpdateAddressAmountRequest]
          .left
          .map(x => UnparseableRequest(x))
      )
      updateRes <- addressesService.updateBitcoinAddressValue(
        addressId,
        BitcoinAddressValue(updateAddressAmount.satoshis)
      )
    } yield MessageResponse("successful update"))
  }

}
