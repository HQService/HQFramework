package kr.hqservice.framework.bukkit.scheduler.database.table

import kr.hqservice.framework.database.component.HQTable
import org.jetbrains.exposed.sql.Table

@HQTable(withLogs = false)
object QuartzSchedulerStateTable : Table("hqframework_quartz_SCHEDULER_STATE") {
    override val primaryKey: PrimaryKey
        get() = PrimaryKey(schedName, instanceName)

    val schedName = varchar("SCHED_NAME", 120)
    val instanceName = varchar("INSTANCE_NAME", 200)
    val lastCheckinTime = long("LAST_CHECKIN_TIME")
    val checkinInterval = long("CHECKIN_INTERVAL")
}