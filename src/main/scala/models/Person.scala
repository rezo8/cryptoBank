package models

import java.util.UUID

// TODO add email validation to constructor
final case class Person(
    id: UUID,
    firstName: String,
    lastName: String,
    email: String,
    phoneNumber: String
)
