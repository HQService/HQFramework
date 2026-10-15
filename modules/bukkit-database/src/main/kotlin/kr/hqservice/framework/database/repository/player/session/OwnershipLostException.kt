package kr.hqservice.framework.database.repository.player.session

import java.util.UUID

class OwnershipLostException(val uuid: UUID) : RuntimeException("ownership of $uuid was lost")
