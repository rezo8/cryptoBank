package repository.mocks

import models.User
import repository.Exceptions.RepositoryException
import repository.{UsersRepository, UsersRepositoryTrait}
import utils.ZioTypes.RezoDBTask
import zio.{URLayer, ZLayer}
import zio.mock.*

import java.util.UUID

object UsersRepositoryMock extends Mock[UsersRepositoryTrait] {
  object GetUser extends Effect[UUID, RepositoryException, User]
  object GetUserByEmail extends Effect[String, RepositoryException, User]
  object CreateUser
      extends Effect[
        (Int, String, String, String, String, String),
        RepositoryException,
        UUID
      ]

  val compose: URLayer[Proxy, UsersRepositoryTrait] =
    ZLayer.fromFunction {
      (proxy: Proxy) => // Explicitly specify the type of `proxy`
        new UsersRepositoryTrait {
          override def getUser(userId: UUID): RezoDBTask[User] =
            proxy(GetUser, userId)

          override def getUserByEmail(email: String): RezoDBTask[User] =
            proxy(GetUserByEmail, email)

          override def createUser(
              userTypeId: Int,
              firstName: String,
              lastName: String,
              email: String,
              phoneNumber: String,
              passwordHash: String
          ): RezoDBTask[UUID] =
            proxy(
              CreateUser,
              (
                userTypeId,
                firstName,
                lastName,
                email,
                phoneNumber,
                passwordHash
              )
            )
        }
    }
}
