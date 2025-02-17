package com.rezo.services

import com.rezo.kafka.producers.{
  CreateUserEventProducer,
  CreateUserEventProducerTrait
}
import com.rezo.models.User
import com.rezo.repository.UsersRepositoryTrait
import com.rezo.services.Exceptions.{ServerException, Unexpected}
import com.rezo.utils.ZioTypes.RezoServerTask
import zio.*

import java.util.UUID

class UsersService(
    usersRepository: UsersRepositoryTrait,
    createUserEventProducer: CreateUserEventProducerTrait
) extends RepositoryService {
  def createUser(
      userTypeId: Int,
      firstName: String,
      lastName: String,
      email: String,
      phoneNumber: String,
      passwordHash: String
  ): RezoServerTask[User] = {
    for {
      user <- usersRepository
        .createUser(
          userTypeId = userTypeId,
          firstName = firstName,
          lastName = lastName,
          email = email,
          phoneNumber = phoneNumber,
          passwordHash = passwordHash
        )
        .mapError(handleRepositoryExceptions)
      _ <- createUserEventProducer
        .produce(user)
        .provideSomeLayer[Any](
          createUserEventProducer.producer
        )
        .mapError(x => Unexpected(x).asInstanceOf[ServerException])
//       .fork .TODO way to improve this. we are unable to properly fork this and show it in a spec.
    } yield user

  }

  def getUserById(userId: UUID): RezoServerTask[User] = {
    usersRepository
      .getUser(userId)
      .mapError(handleRepositoryExceptions)
  }

  def getUserByEmail(email: String): RezoServerTask[User] = {
    usersRepository
      .getUserByEmail(email)
      .mapError(handleRepositoryExceptions)
  }
}

object UsersService {
  val live: URLayer[
    UsersRepositoryTrait & CreateUserEventProducerTrait,
    UsersService
  ] =
    ZLayer.fromFunction {
      (
          usersRepository: UsersRepositoryTrait,
          createUserEventProducer: CreateUserEventProducerTrait
      ) =>
        new UsersService(usersRepository, createUserEventProducer)
    }
}
