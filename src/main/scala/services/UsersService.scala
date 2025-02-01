package services

import models.User
import repository.UsersRepositoryTrait
import utils.ZioTypes.RezoTask
import zio.*

import java.util.UUID

class UsersService(usersRepository: UsersRepositoryTrait)
    extends RepositoryService {

  def createUser(
      userTypeId: Int,
      firstName: String,
      lastName: String,
      email: String,
      phoneNumber: String,
      passwordHash: String
  ): RezoTask[UUID] = {
    usersRepository
      .createUser(
        userTypeId = userTypeId,
        firstName = firstName,
        lastName = lastName,
        email = email,
        phoneNumber = phoneNumber,
        passwordHash = passwordHash
      )
      .mapError(handleRepositoryExceptions)
  }

  def getUserById(userId: UUID): RezoTask[User] = {
    usersRepository
      .getUser(userId)
      .mapError(handleRepositoryExceptions)
  }

  def getUserByEmail(email: String): RezoTask[User] = {
    usersRepository
      .getUserByEmail(email)
      .mapError(handleRepositoryExceptions)
  }
}

object UsersService {
  val live: URLayer[UsersRepositoryTrait, UsersService] =
    ZLayer.fromFunction(new UsersService(_))
}
