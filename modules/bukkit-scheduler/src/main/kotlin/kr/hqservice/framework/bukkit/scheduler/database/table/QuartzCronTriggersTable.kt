package kr.hqservice.framework.bukkit.scheduler.database.table

import kr.hqservice.framework.database.component.HQTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

@HQTable(withLogs = false)
object QuartzCronTriggersTable : Table("hqframework_quartz_CRON_TRIGGERS") {
    override val primaryKey: PrimaryKey
        get() = PrimaryKey(schedName, triggerName, triggerGroup)

    val schedName = reference("SCHED_NAME", QuartzTriggersTable.schedName, ReferenceOption.CASCADE)
    val triggerName = reference("TRIGGER_NAME", QuartzTriggersTable.triggerName, ReferenceOption.CASCADE)
    val triggerGroup = reference("TRIGGER_GROUP", QuartzTriggersTable.triggerGroup, ReferenceOption.CASCADE)
    val cronExpression = varchar("CRON_EXPRESSION", 200)
    val timeZoneId = varchar("TIME_ZONE_ID", 80)
}