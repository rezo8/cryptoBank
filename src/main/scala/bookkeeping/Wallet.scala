package bookkeeping

import java.util.UUID

final case class Wallet(id: UUID, ownerId: UUID, coins: Seq[UUID])
