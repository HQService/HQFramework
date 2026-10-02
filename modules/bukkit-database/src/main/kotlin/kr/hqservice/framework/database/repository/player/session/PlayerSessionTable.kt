package kr.hqservice.framework.database.repository.player.session

import kr.hqservice.framework.database.component.Table
import org.jetbrains.exposed.sql.javatime.timestamp

@Table(withLogs = false)
object PlayerSessionTable : org.jetbrains.exposed.sql.Table("hqframework_player_session") {
    val uuid = uuid("uuid")
    val owner = varchar("owner", 64).nullable()
    val version = long("version").default(0)
    val leaseUntil = timestamp("lease_until").nullable()
    override val primaryKey = PrimaryKey(uuid)
}
