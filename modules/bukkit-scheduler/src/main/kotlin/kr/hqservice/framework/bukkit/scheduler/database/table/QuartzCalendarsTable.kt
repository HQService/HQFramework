package kr.hqservice.framework.bukkit.scheduler.database.table

import org.jetbrains.exposed.sql.Table

object QuartzCalendarsTable : Table("hqframework_QRTZ_CALENDARS"){
    override val primaryKey: PrimaryKey
        get() = PrimaryKey(schedName, calendarName)

    val schedName = varchar("SCHED_NAME", 120)
    val calendarName = varchar("CALENDAR_NAME", 200)
    val calendarBlob = blob("CALENDAR_BLOB")
}