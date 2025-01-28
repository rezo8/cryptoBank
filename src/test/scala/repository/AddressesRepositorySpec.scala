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

  // Shared setup logic
  private def setupUserAccountAndAddress = for {
    user <- ZIO.succeed(UsersFixtures.nextUser())
    uuidEither <- usersRepository.safeCreateUser(
      user.userTypeId,
      user.firstName,
      user.lastName,
      user.email,
      user.phoneNumber,
      user.passwordHash
    )
    userId <- ZIO.fromEither(uuidEither)
    createdAccount <- accountsRepository.safeCreateAccount(
      userId,
      "BTC",
      "test account"
    )
    accountId <- ZIO.fromEither(createdAccount)
    addressId <- addressRepository.createBitcoinAddress(
      accountId,
      addressLocation,
      addressValue
    )
  } yield (user, userId, accountId, addressId)

  private val addressLocation = "Test address loc"
  private val addressValue =
    BitcoinAddressValue.apply(Random.between(0L, 10_000_000L))

  def spec: Spec[Any, Throwable] = suite("AddressesRepositorySpec")(
    test("properly create and load address ") {
      for {
        (_, _, accountId, addressId) <- setupUserAccountAndAddress
        loadedAddressEither <- addressRepository.getAddressByAddressId(
          addressId
        )
        loadedAddress <- ZIO.fromEither(loadedAddressEither)
      } yield assertTrue({
        Address(
          addressId = loadedAddress.addressId,
          accountId = accountId,
          address = addressLocation,
          balance = addressValue.satoshis,
          isActive = true,
          createdAt = loadedAddress.createdAt,
          updatedAt = loadedAddress.updatedAt
        ) == loadedAddress
      })
    },
    test("can update address owned amount") {
      val newValue = BitcoinAddressValue(addressValue.satoshis - 1L)
      for {
        (_, _, accountId, addressId) <- setupUserAccountAndAddress
        _ <- addressRepository.updateBitcoinAddressValue(addressId, newValue)
        loadedAddressEither <- addressRepository.getAddressByAddressId(
          addressId
        )
        loadedAddress <- ZIO.fromEither(loadedAddressEither)
      } yield assertTrue({
        Address(
          addressId = loadedAddress.addressId,
          accountId = accountId,
          address = addressLocation,
          balance = newValue.satoshis,
          isActive = true,
          createdAt = loadedAddress.createdAt,
          updatedAt = loadedAddress.updatedAt
        ) == loadedAddress
      })
    },
    test("can create and load multiple addresses for a user") {
      val secondAddressLoc = "Test address loc 2"
      for {
        (_, _, accountId, addressId) <- setupUserAccountAndAddress
        addressId <- addressRepository.createBitcoinAddress(
          accountId,
          secondAddressLoc,
          addressValue
        )
        loadedAddressesEither <- addressRepository.getAddressesByAccountId(
          accountId
        )
        loadedAddresses <- ZIO.fromEither(loadedAddressesEither)
      } yield assertTrue({
        loadedAddresses.size == 2 && loadedAddresses.exists(
          _.address == addressLocation
        ) && loadedAddresses.exists(_.address == secondAddressLoc)
      })
    },
    test("should fail when account does not exist") {
      for {
        addAddressToAccountRes <- addressRepository
          .createBitcoinAddress(
            UUID.randomUUID(),
            addressLocation,
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
