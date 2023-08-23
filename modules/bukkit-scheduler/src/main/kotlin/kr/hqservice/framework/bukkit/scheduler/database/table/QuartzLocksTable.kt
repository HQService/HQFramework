package kr.hqservice.framework.bukkit.scheduler.database.table

import kr.hqservice.framework.database.component.HQTable
import org.jetbrains.exposed.sql.Table

@HQTable(withLogs = false)
object QuartzLocksTable : Table("hqframework_quartz_LOCKS") {
    override val primaryKey: PrimaryKey
        get() = PrimaryKey(schedName, lockName)

    val schedName = varchar("SCHED_NAME", 120)
    val lockName = varchar("LOCK_NAME", 40)
}