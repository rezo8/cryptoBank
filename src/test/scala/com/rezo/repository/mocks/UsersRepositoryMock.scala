package com.rezo.repository.mocks

import com.rezo.models.User
import com.rezo.repository.Exceptions.RepositoryException
import com.rezo.repository.{UsersRepository, UsersRepositoryTrait}
import com.rezo.utils.ZioTypes.RezoDBTask
import zio.mock.*
import zio.{URLayer, ZLayer}

import java.util.UUID

object UsersRepositoryMock extends Mock[UsersRepositoryTrait] {
  object GetUser extends Effect[UUID, RepositoryException, User]
  object GetUserByEmail extends Effect[String, RepositoryException, User]
  object CreateUser
      extends Effect[
        (Int, String, String, String, String, String),
        RepositoryException,
        User
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
          ): RezoDBTask[User] =
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
