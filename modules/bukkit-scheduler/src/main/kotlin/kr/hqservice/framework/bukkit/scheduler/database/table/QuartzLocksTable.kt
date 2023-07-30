package kr.hqservice.framework.bukkit.scheduler.database.table

import org.jetbrains.exposed.sql.Table

object QuartzLocksTable : Table("hqframework_QRTZ_LOCKS") {
    override val primaryKey: PrimaryKey
        get() = PrimaryKey(schedName, lockName)

    val schedName = varchar("SCHED_NAME", 120)
    val lockName = varchar("LOCK_NAME", 40)
}