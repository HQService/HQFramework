package kr.hqservice.framework.bukkit.scheduler.database.table

import org.jetbrains.exposed.sql.Table

object QuartzSchedulerStateTable : Table("hqframework_QRTZ_SCHEDULER_STATE") {
    override val primaryKey: PrimaryKey
        get() = PrimaryKey(schedName, instanceName)

    val schedName = varchar("SCHED_NAME", 120)
    val instanceName = varchar("INSTANCE_NAME", 200)
    val lastCheckinTime = long("LAST_CHECKIN_TIME")
    val checkinInterval = long("CHECKIN_INTERVAL")
}