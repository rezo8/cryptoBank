package repository

import cats.effect.*
import doobie.util.transactor.Transactor.Aux
import fixtures.UsersFixtures
import models.{Address, BitcoinAddressValue}
import org.postgresql.util.PSQLException
import zio.ZIO
import zio.test.*
import zio.test.Assertion.*

import java.util.UUID
import scala.util.Random

object AddressesRepositorySpec extends ZIOSpecDefault with RepositorySpec {
  val addressRepository: AddressRepository = new AddressRepository:
    override val transactor: Aux[IO, Unit] = testTransactor

  val usersRepository: UsersRepository = new UsersRepository:
    override val transactor: Aux[IO, Unit] = testTransactor
  val accountsRepository: AccountsRepository = new AccountsRepository:
    override val transactor: Aux[IO, Unit] = testTransactor

  def spec: Spec[Any, Throwable] = suite("AddressesRepositorySpec")(
    test("properly create and load address ") {
      val user = UsersFixtures.nextUser()
      val addressLocation = "Test address loc"
      val addressValue =
        BitcoinAddressValue.apply(Random.between(0L, 10_000_000L))
      for {
        uuidEither <- usersRepository.safeCreateUser(
          user.userTypeId,
          user.firstName,
          user.lastName,
          user.email,
          user.phoneNumber,
          user.passwordHash
        )
        userId = uuidEither.getOrElse(throw new Exception())
        createdAccount <- accountsRepository.safeCreateAccount(
          userId,
          "BTC",
          "test account"
        )
        createdAccountId <- ZIO.fromEither(createdAccount)
        _ <- addressRepository.createBitcoinAddress(
          createdAccountId,
          addressLocation,
          addressValue
        )
        loadedAddressEither <- addressRepository.getAddressesByAccountId(
          createdAccountId
        )
        loadedAddresses <- ZIO.fromEither(loadedAddressEither)
      } yield assertTrue({
        val loadedAddress = loadedAddresses.head
        Address(
          addressId = loadedAddress.addressId,
          accountId = createdAccountId,
          address = addressLocation,
          balance = addressValue.satoshis,
          isActive = true,
          createdAt = loadedAddress.createdAt,
          updatedAt = loadedAddress.updatedAt
        ) == loadedAddress
      })
    },
    test("can update address owned amount") {
      val user = UsersFixtures.nextUser()
      val addressId = UUID.randomUUID()
      val addressLocation = "Test Address location"
      val addressValue =
        BitcoinAddressValue.apply(Random.between(0L, 10_000_000L))
      val newValue = BitcoinAddressValue(addressValue.satoshis - 1L)
      for {
        uuidEither <- usersRepository.safeCreateUser(
          user.userTypeId,
          user.firstName,
          user.lastName,
          user.email,
          user.phoneNumber,
          user.passwordHash
        )
        userId = uuidEither.getOrElse(throw new Exception())
        createdAccount <- accountsRepository.safeCreateAccount(
          userId,
          "BTC",
          "test account"
        )
        createdAccountId <- ZIO.fromEither(createdAccount)
        addressId <- addressRepository.createBitcoinAddress(
          createdAccountId,
          addressLocation,
          addressValue
        )
        _ <- addressRepository.updateBitcoinAddressValue(addressId, newValue)
        loadedAddressEither <- addressRepository.getAddressesByAccountId(
          createdAccountId
        )
        loadedAddresses <- ZIO.fromEither(loadedAddressEither)
      } yield assertTrue({
        val loadedAddress = loadedAddresses.head
        Address(
          addressId = loadedAddress.addressId,
          accountId = createdAccountId,
          address = addressLocation,
          balance = newValue.satoshis,
          isActive = true,
          createdAt = loadedAddress.createdAt,
          updatedAt = loadedAddress.updatedAt
        ) == loadedAddress
      })
    },
    test("should fail when address does not exist") {
      val user = UsersFixtures.nextUser()
      val addressId = UUID.randomUUID()
      val addressName = "Test Address"
      val addressValue =
        BitcoinAddressValue.apply(Random.between(0L, 10_000_000L))

      for {
        uuidEither <- usersRepository.safeCreateUser(
          user.userTypeId,
          user.firstName,
          user.lastName,
          user.email,
          user.phoneNumber,
          user.passwordHash
        )
        userId = uuidEither.getOrElse(throw new Exception())
        addAddressToAccountRes <- addressRepository
          .createBitcoinAddress(
            UUID.randomUUID(),
            "test",
            addressValue
          )
          .exit
      } yield assert(addAddressToAccountRes)(
        fails(isSubtype[PSQLException](anything))
      )
    }
  ) @@ TestAspect.beforeAll(initializeDb)
    @@ TestAspect.afterAll(closeDb)
}
