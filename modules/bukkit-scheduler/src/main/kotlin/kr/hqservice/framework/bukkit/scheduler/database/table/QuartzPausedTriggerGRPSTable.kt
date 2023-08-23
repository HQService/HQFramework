package kr.hqservice.framework.bukkit.scheduler.database.table

import kr.hqservice.framework.database.component.HQTable
import org.jetbrains.exposed.sql.Table

@HQTable(withLogs = false)
object QuartzPausedTriggerGRPSTable : Table("hqframework_quartz_PAUSED_TRIGGER_GRPS") {
    override val primaryKey: PrimaryKey
        get() = PrimaryKey(schedName, triggerGroup)

    val schedName = varchar("SCHED_NAME", 120)
    val triggerGroup = varchar("TRIGGER_GROUP", 200)
}