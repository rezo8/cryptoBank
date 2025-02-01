package repository

import fixtures.UsersFixtures
import models.Address
import repository.AddressesRepositorySpec.test
import repository.Exceptions.{
  MissingAccountByAccountId,
  MissingAddressByAddressId,
  UniqueViolationAccountIdAddress
}
import zio.ZIO
import zio.test.*
import zio.test.Assertion.*

import java.util.UUID
import scala.util.Random

// TODO better organize these tests.
object AddressesRepositorySpec extends ZIOSpecDefault with RepositorySpec {
  val accountsRepository: AccountsRepository = new AccountsRepository(
    testTransactor
  )
  val addressRepository: AddressesRepository = new AddressesRepository(
    testTransactor
  )
  val usersRepository: UsersRepository = new UsersRepository(testTransactor)

  // Shared setup logic
  private def setupUserAccountAndAddress = for {
    user <- ZIO.succeed(UsersFixtures.nextUser())
    userId <- usersRepository.createUser(
      user.userTypeId,
      user.firstName,
      user.lastName,
      user.email,
      user.phoneNumber,
      user.passwordHash
    )
    accountId <- accountsRepository.createAccount(
      userId,
      "BTC",
      "test account"
    )
    addressId <- addressRepository.createAddress(
      accountId,
      addressLocation,
      addressValue
    )
  } yield (user, userId, accountId, addressId)

  private val addressLocation = "Test addressName loc"
  private val addressValue = Random.between(0L, 10_000_000L)

  def spec: Spec[Any, Throwable] = suite("AddressesRepositorySpec")(
    test("properly create and load addressName ") {
      for {
        (_, _, accountId, addressId) <- setupUserAccountAndAddress
        loadedAddress <- addressRepository.getAddressByAddressId(addressId)
      } yield assertTrue({
        Address(
          addressId = loadedAddress.addressId,
          accountId = accountId,
          address = addressLocation,
          balance = addressValue,
          isActive = true,
          createdAt = loadedAddress.createdAt,
          updatedAt = loadedAddress.updatedAt
        ) == loadedAddress
      })
    },
    test("can update addressName owned amount") {
      val newValue = addressValue - 1L
      for {
        (_, _, accountId, addressId) <- setupUserAccountAndAddress
        _ <- addressRepository.updateAddressValue(addressId, newValue)
        loadedAddress <- addressRepository.getAddressByAddressId(
          addressId
        )
      } yield assertTrue({
        Address(
          addressId = loadedAddress.addressId,
          accountId = accountId,
          address = addressLocation,
          balance = newValue,
          isActive = true,
          createdAt = loadedAddress.createdAt,
          updatedAt = loadedAddress.updatedAt
        ) == loadedAddress
      })
    },
    test("can create and load multiple addresses for a user") {
      val secondAddressLoc = "Test addressName loc 2"
      for {
        (_, _, accountId, addressId) <- setupUserAccountAndAddress
        addressId <- addressRepository.createAddress(
          accountId,
          secondAddressLoc,
          addressValue
        )
        loadedAddresses <- addressRepository.getAddressesByAccountId(
          accountId
        )
      } yield assertTrue({
        loadedAddresses.size == 2 && loadedAddresses.exists(
          _.address == addressLocation
        ) && loadedAddresses.exists(_.address == secondAddressLoc)
      })
    },
    test("fails with MissingAccountByAccountId when account does not exist") {
      val randomId = UUID.randomUUID()
      assertZIO(
        addressRepository
          .createAddress(
            randomId,
            addressLocation,
            addressValue
          )
          .exit
      )(fails(equalTo(MissingAccountByAccountId(randomId))))
    },
    test(
      "fails with UniqueViolationAccountIdAddress when account already has addressName"
    ) {
      for {
        (_, _, accountId, addressId) <- setupUserAccountAndAddress
        test <- assertZIO(
          addressRepository
            .createAddress(
              accountId,
              addressLocation,
              addressValue
            )
            .exit
        )(
          fails(
            equalTo(UniqueViolationAccountIdAddress(accountId, addressLocation))
          )
        )
      } yield test
    },
    test(
      "fails with MissingAddressByAddressId when addressName does not exist"
    ) {
      val randomId = UUID.randomUUID()
      assertZIO(
        addressRepository
          .getAddressByAddressId(randomId)
          .exit
      )(fails(equalTo(MissingAddressByAddressId(randomId))))
    },
    test(
      "fails with MissingAddressByAddressId when addressName does not exist on update"
    ) {
      val randomId = UUID.randomUUID()
      assertZIO(
        addressRepository
          .updateAddressValue(randomId, 25L)
          .exit
      )(fails(equalTo(MissingAddressByAddressId(randomId))))
    }
  ) @@ TestAspect.beforeAll(initializeDb)
    @@ TestAspect.afterAll(closeDb)
}
