package com.rezo.repository

import java.util.UUID

object Exceptions {
  trait RepositoryException extends Exception
  trait UnexpectedSqlError extends RepositoryException

  trait ExcessiveUpdate extends RepositoryException
  trait UniqueViolation extends RepositoryException
  trait ForeignKeyViolation extends RepositoryException
  trait MissingEntry extends RepositoryException

  case class ExcessiveUpdateAddresses(updateCount: Int)
      extends ExcessiveUpdate {
    override def getMessage: String =
      s"Unexpectedly updated $updateCount addresses. Please verify query."
  }
  case class ForeignKeyViolationUser(userId: UUID) extends ForeignKeyViolation {
    override def getMessage: String =
      s"Foreign key violation. Missing User with id [${userId}]"
  }

  case class MissingUserById(userId: UUID) extends MissingEntry {
    override def getMessage: String =
      s"User with id [${userId.toString}] does not exist"
  }

  case class MissingUserByEmail(email: String) extends MissingEntry {
    override def getMessage: String = s"User with email [$email] does not exist"
  }

  case class MissingAddressByAddressId(addressId: UUID) extends MissingEntry {
    override def getMessage: String =
      s"Address with accountId [${addressId.toString}] does not exist."
  }

  case class MissingAccountByAccountId(accountId: UUID) extends MissingEntry {
    override def getMessage: String =
      s"Account with accountId [$accountId] does not exist."
  }

  case class MissingAccountByUserIdAndCryptoType(
      userId: UUID,
      cryptoType: String
  ) extends MissingEntry {
    override def getMessage: String =
      s"Account with userId [$userId] and cryptoType [${cryptoType}] does not exist."
  }

  case class UnexpectedError(message: String) extends UnexpectedSqlError {
    override def getMessage: String =
      s"SQL code failed with unexpected error: [${message}]"
  }

  case class UniqueViolationUser(
      email: String,
      phoneNumber: String
  ) extends UniqueViolation {
    override def getMessage: String =
      s"User with email [$email] or phoneNumber [$phoneNumber] already exists"
  }

  case class UniqueViolationAccountIdAddress(
      accountId: UUID,
      address: String
  ) extends UniqueViolation {
    override def getMessage: String =
      s"Account [${accountId.toString}] already has address [${address}]"
  }

  case class UniqueViolationUserCryptoType(
      userId: UUID,
      cryptoType: String
  ) extends UniqueViolation {
    override def getMessage: String =
      s"Account already exists for User [${userId.toString}] with cryptoType [${cryptoType}]"
  }

}
