package repository.mocks

import com.rezo.models.Address
import com.rezo.repository.AddressesRepositoryTrait
import com.rezo.repository.Exceptions.RepositoryException
import com.rezo.utils.ZioTypes.RezoDBTask
import zio.mock.*
import zio.{URLayer, ZLayer}

import java.util.UUID

object AddressesRepositoryMock extends Mock[AddressesRepositoryTrait] {
  object GetAddressesByAccountId
      extends Effect[UUID, RepositoryException, List[Address]]
  object GetAddressByAddressId
      extends Effect[UUID, RepositoryException, Address]
  object CreateAddress
      extends Effect[
        (UUID, String, Long),
        RepositoryException,
        UUID
      ]

  object UpdateAddressValue
      extends Effect[(UUID, Long), RepositoryException, Int]

  val compose: URLayer[Proxy, AddressesRepositoryTrait] =
    ZLayer.fromFunction {
      (proxy: Proxy) => // Explicitly specify the type of `proxy`
        new AddressesRepositoryTrait {

          override def createAddress(
              accountId: UUID,
              address: String,
              balance: Long
          ): RezoDBTask[UUID] =
            proxy(
              CreateAddress,
              (
                accountId,
                address,
                balance
              )
            )
          override def getAddressesByAccountId(
              accountId: UUID
          ): RezoDBTask[List[Address]] =
            proxy(GetAddressesByAccountId, accountId)

          override def getAddressByAddressId(
              addressId: UUID
          ): RezoDBTask[Address] =
            proxy(GetAddressByAddressId, addressId)

          override def updateAddressValue(
              addressId: UUID,
              addressValue: Long
          ): RezoDBTask[Int] =
            proxy(UpdateAddressValue, (addressId, addressValue))

        }

    }
}
