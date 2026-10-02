package kr.hqservice.framework.database.repository.player.session

import java.util.UUID

class OwnershipTimeoutException(val uuid: UUID, cause: Throwable? = null) :
    RuntimeException("could not acquire ownership of $uuid in time", cause)
