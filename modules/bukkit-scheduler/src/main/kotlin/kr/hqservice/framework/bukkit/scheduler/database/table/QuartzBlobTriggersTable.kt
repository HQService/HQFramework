package kr.hqservice.framework.bukkit.scheduler.database.table

import kr.hqservice.framework.database.component.HQTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

@HQTable(withLogs = false)
object QuartzBlobTriggersTable : Table("hqframework_quartz_BLOB_TRIGGERS") {
    override val primaryKey: PrimaryKey
        get() = PrimaryKey(schedName, triggerName, triggerGroup)

    val schedName = reference("SCHED_NAME", QuartzTriggersTable.schedName, ReferenceOption.CASCADE)
    val triggerName = reference("TRIGGER_NAME", QuartzTriggersTable.triggerName, ReferenceOption.CASCADE)
    val triggerGroup = reference("TRIGGER_GROUP", QuartzTriggersTable.triggerGroup, ReferenceOption.CASCADE)
    val blobData = blob("BLOB_DATA").nullable()
}