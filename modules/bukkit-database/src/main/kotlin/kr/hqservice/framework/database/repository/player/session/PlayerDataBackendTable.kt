package kr.hqservice.framework.database.repository.player.session

import kr.hqservice.framework.database.component.Table

@Table(withLogs = false)
object PlayerDataBackendTable : org.jetbrains.exposed.sql.Table("hqframework_player_data_backend") {
    val id = integer("id")
    val backend = varchar("backend", 16)
    override val primaryKey = PrimaryKey(id)
}
