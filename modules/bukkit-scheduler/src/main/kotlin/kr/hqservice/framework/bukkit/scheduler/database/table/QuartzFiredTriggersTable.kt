package kr.hqservice.framework.bukkit.scheduler.database.table

import org.jetbrains.exposed.sql.Table

object QuartzFiredTriggersTable : Table("hqframework_QRTZ_FIRED_TRIGGERS") {
    override val primaryKey: PrimaryKey
        get() = PrimaryKey(schedName, entryId)

    val schedName = varchar("SCHED_NAME", 120)
    val entryId = varchar("ENTRY_ID", 95)
    val triggerName = varchar("TRIGGER_NAME", 200)
    val triggerGroup = varchar("TRIGGER_GROUP", 200)
    val instanceName = varchar("INSTANCE_NAME", 200)
    val firedTime = long("FIRE_TIME")
    val schedTime = long("SCHED_TIME")
    val priority = integer("PRIORITY")
    val state = varchar("STATE", 16)
    val jobName = varchar("JOB_NAME", 200).nullable()
    val jobGroup = varchar("JOB_GROUP", 200).nullable()
    val isNonConcurrent = varchar("IS_NONCONCURRENT", 1).nullable()
    val requestsRecovery = varchar("REQUESTS_RECOVERY", 1).nullable()
}