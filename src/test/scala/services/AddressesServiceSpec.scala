package services

import fixtures.RepositoryFixtures.nextAddress
import models.BitcoinAddressValue
import repository.AddressesRepositoryTrait
import repository.Exceptions.*
import repository.mocks.AddressesRepositoryMock
import services.Exceptions.{
  DatabaseConflict,
  MissingDatabaseObject,
  Unexpected,
  UnexpectedUpdate
}
import zio.*
import zio.mock.Expectation.*
import zio.test.*
import zio.test.Assertion.*

import java.util.UUID

object AddressesServiceSpec extends ZIOSpecDefault {

  val accountId: UUID = UUID.randomUUID()
  val addressName = "test cryptoType"
  val balance = BitcoinAddressValue(math.random().toLong)

  val defaultAddress = nextAddress()

  def spec: Spec[Any, Throwable] = suite("AddressesServiceSpec")(
    suite("#createBitCoinAddress") {
      val program = for {
        addressesService <- ZIO.service[AddressesService]
        result <- addressesService.createBitcoinAddress(
          accountId,
          addressName,
          balance
        )
      } yield result
      Seq(
        test("returns a UUID on success") {
          val mockEnv: ULayer[AddressesRepositoryTrait] =
            AddressesRepositoryMock
              .CreateAddress(
                equalTo(
                  (
                    accountId,
                    addressName,
                    balance.satoshis
                  )
                ),
                value(defaultAddress.addressId)
              )
              .toLayer

          assertZIO(program.provideLayer(mockEnv >>> AddressesService.live))(
            equalTo(defaultAddress.addressId)
          )
        },
        test("fails with UniqueViolation response on UniqueViolationUser") {
          val uniqueViolationAddress =
            UniqueViolationAccountIdAddress(accountId, addressName)
          val mockEnv: ULayer[AddressesRepositoryTrait] =
            AddressesRepositoryMock
              .CreateAddress(
                equalTo(
                  (
                    accountId,
                    addressName,
                    balance.satoshis
                  )
                ),
                failure(uniqueViolationAddress)
              )
              .toLayer

          assertZIO(program.exit)(
            fails(
              equalTo(DatabaseConflict(uniqueViolationAddress.getMessage))
            )
          ).provideLayer(mockEnv >>> AddressesService.live)
        },
        test(
          "fails with MissingDatabaseObject response on MissingAccountByAccountId error"
        ) {
          val missingAccountException = MissingAccountByAccountId(accountId)
          val mockEnv: ULayer[AddressesRepositoryTrait] =
            AddressesRepositoryMock
              .CreateAddress(
                equalTo(
                  (
                    accountId,
                    addressName,
                    balance.satoshis
                  )
                ),
                failure(missingAccountException)
              )
              .toLayer

          assertZIO(program.exit)(
            fails(
              equalTo(MissingDatabaseObject(missingAccountException.getMessage))
            )
          ).provideLayer(mockEnv >>> AddressesService.live)
        },
        test("fails with Unexpected response on unexpected error") {
          val randomException = UnexpectedError("test")

          val mockEnv: ULayer[AddressesRepositoryTrait] =
            AddressesRepositoryMock
              .CreateAddress(
                equalTo(
                  (
                    accountId,
                    addressName,
                    balance.satoshis
                  )
                ),
                failure(randomException)
              )
              .toLayer

          assertZIO(program.exit)(fails(equalTo(Unexpected(randomException))))
            .provideLayer(mockEnv >>> AddressesService.live)
        }
      )
    },
    suite("getAddressByAddressId") {
      val program = for {
        addressesService <- ZIO.service[AddressesService]
        result <- addressesService.getAddressByAddressId(
          defaultAddress.addressId
        )
      } yield result
      Seq(
        test("returns an Address on success") {
          val mockEnv: ULayer[AddressesRepositoryTrait] =
            AddressesRepositoryMock
              .GetAddressByAddressId(
                equalTo(defaultAddress.addressId),
                value(defaultAddress)
              )
              .toLayer

          assertZIO(program.provideLayer(mockEnv >>> AddressesService.live))(
            equalTo(defaultAddress)
          )
        },
        test("fails with MissingDatabaseObject on MissingUserByEmail error") {
          val missingAddressByAddressId =
            MissingAddressByAddressId(defaultAddress.addressId)
          val mockEnv: ULayer[AddressesRepositoryTrait] =
            AddressesRepositoryMock
              .GetAddressByAddressId(
                equalTo(defaultAddress.addressId),
                failure(missingAddressByAddressId)
              )
              .toLayer

          assertZIO(program.exit)(
            fails(
              equalTo(
                MissingDatabaseObject(missingAddressByAddressId.getMessage)
              )
            )
          ).provideLayer(mockEnv >>> AddressesService.live)
        },
        test("fails with Unexpected response on unexpected error") {
          val randomException = UnexpectedError("test")
          val mockEnv: ULayer[AddressesRepositoryTrait] =
            AddressesRepositoryMock
              .GetAddressByAddressId(
                equalTo(defaultAddress.addressId),
                failure(randomException)
              )
              .toLayer

          assertZIO(program.exit)(fails(equalTo(Unexpected(randomException))))
            .provideLayer(mockEnv >>> AddressesService.live)
        }
      )
    },
    suite("getAddressesByAccountId") {
      val program = for {
        addressesService <- ZIO.service[AddressesService]
        result <- addressesService.getAddressesByAccountId(
          defaultAddress.accountId
        )
      } yield result
      Seq(
        test("returns an Address on success") {
          val expectedList = List(defaultAddress, nextAddress())
          val mockEnv: ULayer[AddressesRepositoryTrait] =
            AddressesRepositoryMock
              .GetAddressesByAccountId(
                equalTo(defaultAddress.accountId),
                value(expectedList)
              )
              .toLayer

          assertZIO(program.provideLayer(mockEnv >>> AddressesService.live))(
            equalTo(expectedList)
          )
        },
        test("fails with Unexpected response on unexpected error") {
          val randomException = UnexpectedError("test")
          val mockEnv: ULayer[AddressesRepositoryTrait] =
            AddressesRepositoryMock
              .GetAddressesByAccountId(
                equalTo(defaultAddress.accountId),
                failure(randomException)
              )
              .toLayer

          assertZIO(program.exit)(fails(equalTo(Unexpected(randomException))))
            .provideLayer(mockEnv >>> AddressesService.live)
        }
      )
    },
    suite("updateAddressValue") {
      val program = for {
        addressesService <- ZIO.service[AddressesService]
        result <- addressesService.updateBitcoinAddressValue(
          defaultAddress.addressId,
          balance
        )
      } yield result
      Seq(
        test("returns an Address on success") {
          val mockEnv: ULayer[AddressesRepositoryTrait] =
            AddressesRepositoryMock
              .UpdateAddressValue(
                equalTo(defaultAddress.addressId, balance.satoshis),
                value(1)
              )
              .toLayer

          assertZIO(program.provideLayer(mockEnv >>> AddressesService.live))(
            equalTo(1)
          )
        },
        test(
          "fails with Missing Database Exception on MissingAddressByAddressId"
        ) {
          val missingAddressByAddressId =
            MissingAddressByAddressId(defaultAddress.addressId)
          val mockEnv: ULayer[AddressesRepositoryTrait] =
            AddressesRepositoryMock
              .UpdateAddressValue(
                equalTo(defaultAddress.addressId, balance.satoshis),
                failure(missingAddressByAddressId)
              )
              .toLayer

          assertZIO(program.exit)(
            fails(
              equalTo(
                MissingDatabaseObject(missingAddressByAddressId.getMessage)
              )
            )
          )
            .provideLayer(mockEnv >>> AddressesService.live)
        },
        test(
          "fails with UnexpectedUpdate Exception on ExcessiveUpdateAddresses"
        ) {
          val excessiveUpdateAddresses = ExcessiveUpdateAddresses(15)
          val mockEnv: ULayer[AddressesRepositoryTrait] =
            AddressesRepositoryMock
              .UpdateAddressValue(
                equalTo(defaultAddress.addressId, balance.satoshis),
                failure(excessiveUpdateAddresses)
              )
              .toLayer

          assertZIO(program.exit)(
            fails(
              equalTo(UnexpectedUpdate(excessiveUpdateAddresses.getMessage))
            )
          ).provideLayer(mockEnv >>> AddressesService.live)
        },
        test("fails with Unexpected response on unexpected error") {
          val randomException = UnexpectedError("test")
          val mockEnv: ULayer[AddressesRepositoryTrait] =
            AddressesRepositoryMock
              .UpdateAddressValue(
                equalTo(defaultAddress.addressId, balance.satoshis),
                failure(randomException)
              )
              .toLayer

          assertZIO(program.exit)(fails(equalTo(Unexpected(randomException))))
            .provideLayer(mockEnv >>> AddressesService.live)
        }
      )
    }
  )
}
