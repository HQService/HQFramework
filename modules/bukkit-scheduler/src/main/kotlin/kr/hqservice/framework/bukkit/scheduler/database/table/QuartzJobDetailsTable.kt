package kr.hqservice.framework.bukkit.scheduler.database.table

import org.jetbrains.exposed.sql.Table

object QuartzJobDetailsTable : Table("hqframework_QRTZ_JOB_DETAILS") {
    override val primaryKey: PrimaryKey
        get() = PrimaryKey(schedName, jobName, jobGroup)

    val schedName = varchar("SCHED_NAME", 120).index()
    val jobName = varchar("JOB_NAME", 200).index()
    val jobGroup = varchar("JOB_GROUP", 200).index()
    val description = varchar("DESCRIPTION", 250).nullable()
    val jobClassName = varchar("JOB_CLASS_NAME", 250)
    val isDurable = varchar("IS_DURABLE", 1)
    val isNonConcurrent = varchar("IS_NONCONCURRENT", 1)
    val isUpdateData = varchar("IS_UPDATE_DATA", 1)
    val requestsRecovery = varchar("REQUESTS_RECOVERY", 1)
    val jobData = blob("JOB_DATA").nullable()
}