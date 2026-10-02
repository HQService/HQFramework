package kr.hqservice.framework.database.repository.player.lifecycle

enum class FlushReason { DIRTY, FULL, IMMEDIATE, EXPLICIT, QUIT, TEARDOWN }
