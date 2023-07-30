package kr.hqservice.framework.bukkit.scheduler.database.table

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object QuartzSimpropTriggersTable : Table("hqframework_QRTZ_SIMPROP_TRIGGERS") {
    override val primaryKey: PrimaryKey
        get() = PrimaryKey(schedName, triggerName, triggerGroup)

    val schedName = reference("SCHED_NAME", QuartzTriggersTable.schedName, ReferenceOption.CASCADE)
    val triggerName = reference("TRIGGER_NAME", QuartzTriggersTable.triggerName, ReferenceOption.CASCADE)
    val triggerGroup = reference("TRIGGER_GROUP", QuartzTriggersTable.triggerGroup, ReferenceOption.CASCADE)
    val strProp1 = varchar("STR_PROP_1", 512).nullable()
    val strProp2 = varchar("STR_PROP_2", 512).nullable()
    val strProp3 = varchar("STR_PROP_3", 512).nullable()
    val intProp1 = integer("INT_PROP_1").nullable()
    val intProp2 = integer("INT_PROP_2").nullable()
    val longProp1 = long("LONG_PROP_1").nullable()
    val longProp2 = long("LONG_PROP_2").nullable()
    val decProp1 = decimal("DEC_PROP_1", 13, 4).nullable()
    val decProp2 = decimal("DEC_PROP_2", 13, 4).nullable()
    val boolProp1 = varchar("BOOL_PROP_1", 1).nullable()
    val boolProp2 = varchar("BOOL_PROP_2", 1).nullable()
}