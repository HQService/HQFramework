package kr.hqservice.framework.bukkit.scheduler.database.table

import kr.hqservice.framework.database.component.HQTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

@HQTable(withLogs = false)
object QuartzTriggersTable : Table("hqframework_quartz_TRIGGERS") {
    override val primaryKey: PrimaryKey
        get() = PrimaryKey(schedName, triggerName, triggerGroup)

    val schedName = reference("SCHED_NAME", QuartzJobDetailsTable.schedName)
    val triggerName = varchar("TRIGGER_NAME", 200).index()
    val triggerGroup = varchar("TRIGGER_GROUP", 200).index()
    val jobName = reference("JOB_NAME", QuartzJobDetailsTable.jobName)
    val jobGroup = reference("JOB_GROUP", QuartzJobDetailsTable.jobGroup)
    val description = varchar("DESCRIPTION", 250).nullable()
    val nextFireTime = long("NEXT_FIRE_TIME").nullable()
    val prevFireTime = long("PREV_FIRE_TIME").nullable()
    val priority = integer("PRIORITY").nullable()
    val triggerState = varchar("TRIGGER_STATE", 16)
    val triggerType = varchar("TRIGGER_TYPE", 8)
    val startTime = long("START_TIME")
    val endTime = long("END_TIME").nullable()
    val calendarName = varchar("CALENDAR_NAME", 200).nullable()
    val misfireInstr = short("MISFIRE_INSTR").nullable()
    val jobData = blob("JOB_DATA").nullable()
}