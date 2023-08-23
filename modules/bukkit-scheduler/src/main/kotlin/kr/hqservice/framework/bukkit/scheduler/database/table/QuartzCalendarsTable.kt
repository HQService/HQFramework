package kr.hqservice.framework.bukkit.scheduler.database.table

import kr.hqservice.framework.database.component.HQTable
import org.jetbrains.exposed.sql.Table

@HQTable(withLogs = false)
object QuartzCalendarsTable : Table("hqframework_quartz_CALENDARS"){
    override val primaryKey: PrimaryKey
        get() = PrimaryKey(schedName, calendarName)

    val schedName = varchar("SCHED_NAME", 120)
    val calendarName = varchar("CALENDAR_NAME", 200)
    val calendarBlob = blob("CALENDAR_BLOB")
}