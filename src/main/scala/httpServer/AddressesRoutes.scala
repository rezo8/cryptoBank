package httpServer

import httpServer.Requests.{CreateAddressRequest, UpdateAddressAmountRequest}
import httpServer.Responses.*
import models.BitcoinAddressValue
import services.AddressesService
import zio.*
import zio.http.*

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
    handleServerResponseWithRequest[
      CreateAddressRequest,
      CreateAddressResponse
    ](
      req,
      (createAddressRequest: CreateAddressRequest) => {
        addressesService
          .createBitcoinAddress(
            createAddressRequest.accountId,
            createAddressRequest.addressLocation,
            BitcoinAddressValue(
              createAddressRequest.balance
            )
          )
          .map(CreateAddressResponse(_))
      }
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
    handleServerResponseWithRequest[
      UpdateAddressAmountRequest,
      MessageResponse
    ](
      req,
      (updateAddressAmountRequest: UpdateAddressAmountRequest) => {
        addressesService
          .updateBitcoinAddressValue(
            addressId,
            BitcoinAddressValue(updateAddressAmountRequest.satoshis)
          )
          .map(_ => MessageResponse("successful update"))
      }
    )
  }

}
