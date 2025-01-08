package kr.hqservice.framework.bukkit.scheduler.database

import kr.hqservice.framework.bukkit.core.component.module.Module
import kr.hqservice.framework.bukkit.core.component.module.Setup
import kr.hqservice.framework.database.datasource.H2DataSource
import kr.hqservice.framework.database.datasource.MySQLDataSource
import kr.hqservice.framework.database.datasource.SQLiteDataSource
import kr.hqservice.framework.global.core.util.AnsiColor
import java.sql.ResultSet
import java.util.logging.Logger
import javax.sql.DataSource

@Suppress("SqlSourceToSinkFlow", "DuplicatedCode")
@Module
class HQFrameworkSchedulerTableInitializer(private val dataSource: DataSource, private val logger: Logger) {
    @Suppress("SqlDialectInspection", "SqlNoDataSourceInspection", "IdentifierGrammar")
    private interface QuerySet {
        val showTables: String
        val dropFiredTriggersTable: String
            get() = "DROP TABLE IF EXISTS hqframework_quartz_FIRED_TRIGGERS;"
        val dropPausedTriggerGRPSTable: String
            get() = "DROP TABLE IF EXISTS hqframework_quartz_PAUSED_TRIGGER_GRPS;"
        val dropSchedulerStateTable: String
            get() = "DROP TABLE IF EXISTS hqframework_quartz_SCHEDULER_STATE;"
        val dropLocksTable: String
            get() = "DROP TABLE IF EXISTS hqframework_quartz_LOCKS;"
        val dropSimpleTriggersTable: String
            get() = "DROP TABLE IF EXISTS hqframework_quartz_SIMPLE_TRIGGERS;"
        val dropSimpropTriggersTable: String
            get() = "DROP TABLE IF EXISTS hqframework_quartz_SIMPROP_TRIGGERS;"
        val dropCronTriggersTable: String
            get() = "DROP TABLE IF EXISTS hqframework_quartz_CRON_TRIGGERS;"
        val dropBlobTriggersTable: String
            get() = "DROP TABLE IF EXISTS hqframework_quartz_BLOB_TRIGGERS;"
        val dropTriggersTable: String
            get() = "DROP TABLE IF EXISTS hqframework_quartz_TRIGGERS;"
        val dropJobDetailsTable: String
            get() = "DROP TABLE IF EXISTS hqframework_quartz_JOB_DETAILS;"
        val dropCalendarsTable: String
            get() = "DROP TABLE IF EXISTS hqframework_quartz_CALENDARS;"
        val createJobDetailsTable: String
        val createTriggersTable: String
        val createSimpleTriggersTable: String
        val createCronTriggersTable: String
        val createSimpropTriggersTable: String
        val createBlobTriggersTable: String
        val createCalendarsTable: String
        val createPausedTriggersGRPSTable: String
        val createFiredTriggersTable: String
        val createSchedulerStateTable: String
        val createLocksTable: String
        val indexes: List<String>

        object SQLite : QuerySet {
            override val showTables: String
                get() = "SELECT name FROM sqlite_master WHERE type ='table' AND name LIKE 'hqframework_quartz%'"
            override val createJobDetailsTable: String
                get() = "CREATE TABLE hqframework_quartz_JOB_DETAILS(\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "JOB_NAME VARCHAR(190) NOT NULL,\n" +
                        "JOB_GROUP VARCHAR(190) NOT NULL,\n" +
                        "DESCRIPTION VARCHAR(250) NULL,\n" +
                        "JOB_CLASS_NAME VARCHAR(250) NOT NULL,\n" +
                        "IS_DURABLE VARCHAR(1) NOT NULL,\n" +
                        "IS_NONCONCURRENT VARCHAR(1) NOT NULL,\n" +
                        "IS_UPDATE_DATA VARCHAR(1) NOT NULL,\n" +
                        "REQUESTS_RECOVERY VARCHAR(1) NOT NULL,\n" +
                        "JOB_DATA BLOB NULL,\n" +
                        "PRIMARY KEY (SCHED_NAME,JOB_NAME,JOB_GROUP));"
            override val createTriggersTable: String
                get() = "CREATE TABLE hqframework_quartz_TRIGGERS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "TRIGGER_NAME VARCHAR(190) NOT NULL,\n" +
                        "TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                        "JOB_NAME VARCHAR(190) NOT NULL,\n" +
                        "JOB_GROUP VARCHAR(190) NOT NULL,\n" +
                        "DESCRIPTION VARCHAR(250) NULL,\n" +
                        "NEXT_FIRE_TIME BIGINT(13) NULL,\n" +
                        "PREV_FIRE_TIME BIGINT(13) NULL,\n" +
                        "PRIORITY INTEGER NULL,\n" +
                        "TRIGGER_STATE VARCHAR(16) NOT NULL,\n" +
                        "TRIGGER_TYPE VARCHAR(8) NOT NULL,\n" +
                        "START_TIME BIGINT(13) NOT NULL,\n" +
                        "END_TIME BIGINT(13) NULL,\n" +
                        "CALENDAR_NAME VARCHAR(190) NULL,\n" +
                        "MISFIRE_INSTR SMALLINT(2) NULL,\n" +
                        "JOB_DATA BLOB NULL,\n" +
                        "PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),\n" +
                        "FOREIGN KEY (SCHED_NAME,JOB_NAME,JOB_GROUP)\n" +
                        "REFERENCES hqframework_quartz_JOB_DETAILS(SCHED_NAME,JOB_NAME,JOB_GROUP));"
            override val createSimpleTriggersTable: String
                get() = "CREATE TABLE hqframework_quartz_SIMPLE_TRIGGERS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "TRIGGER_NAME VARCHAR(190) NOT NULL,\n" +
                        "TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                        "REPEAT_COUNT BIGINT(7) NOT NULL,\n" +
                        "REPEAT_INTERVAL BIGINT(12) NOT NULL,\n" +
                        "TIMES_TRIGGERED BIGINT(10) NOT NULL,\n" +
                        "PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),\n" +
                        "FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)\n" +
                        "REFERENCES hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP));"
            override val createCronTriggersTable: String
                get() = "CREATE TABLE hqframework_quartz_CRON_TRIGGERS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "TRIGGER_NAME VARCHAR(190) NOT NULL,\n" +
                        "TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                        "CRON_EXPRESSION VARCHAR(120) NOT NULL,\n" +
                        "TIME_ZONE_ID VARCHAR(80),\n" +
                        "PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),\n" +
                        "FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)\n" +
                        "REFERENCES hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP));"
            override val createSimpropTriggersTable: String
                get() = "CREATE TABLE hqframework_quartz_SIMPROP_TRIGGERS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "TRIGGER_NAME VARCHAR(190) NOT NULL,\n" +
                        "TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                        "STR_PROP_1 VARCHAR(512) NULL,\n" +
                        "STR_PROP_2 VARCHAR(512) NULL,\n" +
                        "STR_PROP_3 VARCHAR(512) NULL,\n" +
                        "INT_PROP_1 INT NULL,\n" +
                        "INT_PROP_2 INT NULL,\n" +
                        "LONG_PROP_1 BIGINT NULL,\n" +
                        "LONG_PROP_2 BIGINT NULL,\n" +
                        "DEC_PROP_1 NUMERIC(13,4) NULL,\n" +
                        "DEC_PROP_2 NUMERIC(13,4) NULL,\n" +
                        "BOOL_PROP_1 VARCHAR(1) NULL,\n" +
                        "BOOL_PROP_2 VARCHAR(1) NULL,\n" +
                        "PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),\n" +
                        "FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)\n" +
                        "REFERENCES hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP));"
            override val createBlobTriggersTable: String
                get() = "CREATE TABLE hqframework_quartz_BLOB_TRIGGERS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "TRIGGER_NAME VARCHAR(190) NOT NULL,\n" +
                        "TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                        "BLOB_DATA BLOB NULL,\n" +
                        "PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),\n" +
                        "FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)\n" +
                        "REFERENCES hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP));"
            override val createCalendarsTable: String
                get() = "CREATE TABLE hqframework_quartz_CALENDARS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "CALENDAR_NAME VARCHAR(190) NOT NULL,\n" +
                        "CALENDAR BLOB NOT NULL,\n" +
                        "PRIMARY KEY (SCHED_NAME,CALENDAR_NAME));\n"
            override val createPausedTriggersGRPSTable: String
                get() = "CREATE TABLE hqframework_quartz_PAUSED_TRIGGER_GRPS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                        "PRIMARY KEY (SCHED_NAME,TRIGGER_GROUP));"
            override val createFiredTriggersTable: String
                get() = "CREATE TABLE hqframework_quartz_FIRED_TRIGGERS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "ENTRY_ID VARCHAR(95) NOT NULL,\n" +
                        "TRIGGER_NAME VARCHAR(190) NOT NULL,\n" +
                        "TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                        "INSTANCE_NAME VARCHAR(190) NOT NULL,\n" +
                        "FIRED_TIME BIGINT(13) NOT NULL,\n" +
                        "SCHED_TIME BIGINT(13) NOT NULL,\n" +
                        "PRIORITY INTEGER NOT NULL,\n" +
                        "STATE VARCHAR(16) NOT NULL,\n" +
                        "JOB_NAME VARCHAR(190) NULL,\n" +
                        "JOB_GROUP VARCHAR(190) NULL,\n" +
                        "IS_NONCONCURRENT VARCHAR(1) NULL,\n" +
                        "REQUESTS_RECOVERY VARCHAR(1) NULL,\n" +
                        "PRIMARY KEY (SCHED_NAME,ENTRY_ID));"
            override val createSchedulerStateTable: String
                get() = "CREATE TABLE hqframework_quartz_SCHEDULER_STATE (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "INSTANCE_NAME VARCHAR(190) NOT NULL,\n" +
                        "LAST_CHECKIN_TIME BIGINT(13) NOT NULL,\n" +
                        "CHECKIN_INTERVAL BIGINT(13) NOT NULL,\n" +
                        "PRIMARY KEY (SCHED_NAME,INSTANCE_NAME));"
            override val createLocksTable: String
                get() = "CREATE TABLE hqframework_quartz_LOCKS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "LOCK_NAME VARCHAR(40) NOT NULL,\n" +
                        "PRIMARY KEY (SCHED_NAME,LOCK_NAME));"
            override val indexes: List<String>
                get() = listOf(
                        "CREATE INDEX IDX_hqframework_quartz_J_REQ_RECOVERY ON hqframework_quartz_JOB_DETAILS(SCHED_NAME,REQUESTS_RECOVERY);",
                        "CREATE INDEX IDX_hqframework_quartz_J_GRP ON hqframework_quartz_JOB_DETAILS(SCHED_NAME,JOB_GROUP);",
                        "CREATE INDEX IDX_hqframework_quartz_T_J ON hqframework_quartz_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);",
                        "CREATE INDEX IDX_hqframework_quartz_T_JG ON hqframework_quartz_TRIGGERS(SCHED_NAME,JOB_GROUP);",
                        "CREATE INDEX IDX_hqframework_quartz_T_C ON hqframework_quartz_TRIGGERS(SCHED_NAME,CALENDAR_NAME);",
                        "CREATE INDEX IDX_hqframework_quartz_T_G ON hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);",
                        "CREATE INDEX IDX_hqframework_quartz_T_STATE ON hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_STATE);",
                        "CREATE INDEX IDX_hqframework_quartz_T_N_STATE ON hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_STATE);",
                        "CREATE INDEX IDX_hqframework_quartz_T_N_G_STATE ON hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_GROUP,TRIGGER_STATE);",
                        "CREATE INDEX IDX_hqframework_quartz_T_NEXT_FIRE_TIME ON hqframework_quartz_TRIGGERS(SCHED_NAME,NEXT_FIRE_TIME);",
                        "CREATE INDEX IDX_hqframework_quartz_T_NFT_ST ON hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_STATE,NEXT_FIRE_TIME);",
                        "CREATE INDEX IDX_hqframework_quartz_T_NFT_MISFIRE ON hqframework_quartz_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME);",
                        "CREATE INDEX IDX_hqframework_quartz_T_NFT_ST_MISFIRE ON hqframework_quartz_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_STATE);",
                        "CREATE INDEX IDX_hqframework_quartz_T_NFT_ST_MISFIRE_GRP ON hqframework_quartz_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_GROUP,TRIGGER_STATE);",
                        "CREATE INDEX IDX_hqframework_quartz_FT_TRIG_INST_NAME ON hqframework_quartz_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME);",
                        "CREATE INDEX IDX_hqframework_quartz_FT_INST_JOB_REQ_RCVRY ON hqframework_quartz_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME,REQUESTS_RECOVERY);",
                        "CREATE INDEX IDX_hqframework_quartz_FT_J_G ON hqframework_quartz_FIRED_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);",
                        "CREATE INDEX IDX_hqframework_quartz_FT_JG ON hqframework_quartz_FIRED_TRIGGERS(SCHED_NAME,JOB_GROUP);",
                        "CREATE INDEX IDX_hqframework_quartz_FT_T_G ON hqframework_quartz_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);",
                        "CREATE INDEX IDX_hqframework_quartz_FT_TG ON hqframework_quartz_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);",
                        "CREATE INDEX IDX_hqframework_quartz_BT_T_G ON hqframework_quartz_BLOB_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);"
                )
        }

        object InnoDB : QuerySet {
            override val showTables: String
                get() = "SHOW TABLES LIKE 'hqframework_quartz%'"
            override val createJobDetailsTable: String
                get() = "CREATE TABLE hqframework_quartz_JOB_DETAILS(\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "JOB_NAME VARCHAR(190) NOT NULL,\n" +
                        "JOB_GROUP VARCHAR(190) NOT NULL,\n" +
                        "DESCRIPTION VARCHAR(250) NULL,\n" +
                        "JOB_CLASS_NAME VARCHAR(250) NOT NULL,\n" +
                        "IS_DURABLE VARCHAR(1) NOT NULL,\n" +
                        "IS_NONCONCURRENT VARCHAR(1) NOT NULL,\n" +
                        "IS_UPDATE_DATA VARCHAR(1) NOT NULL,\n" +
                        "REQUESTS_RECOVERY VARCHAR(1) NOT NULL,\n" +
                        "JOB_DATA BLOB NULL,\n" +
                        "PRIMARY KEY (SCHED_NAME,JOB_NAME,JOB_GROUP))\n" +
                        "ENGINE=InnoDB;"
            override val createTriggersTable: String
                get() = "CREATE TABLE hqframework_quartz_TRIGGERS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "TRIGGER_NAME VARCHAR(190) NOT NULL,\n" +
                        "TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                        "JOB_NAME VARCHAR(190) NOT NULL,\n" +
                        "JOB_GROUP VARCHAR(190) NOT NULL,\n" +
                        "DESCRIPTION VARCHAR(250) NULL,\n" +
                        "NEXT_FIRE_TIME BIGINT(13) NULL,\n" +
                        "PREV_FIRE_TIME BIGINT(13) NULL,\n" +
                        "PRIORITY INTEGER NULL,\n" +
                        "TRIGGER_STATE VARCHAR(16) NOT NULL,\n" +
                        "TRIGGER_TYPE VARCHAR(8) NOT NULL,\n" +
                        "START_TIME BIGINT(13) NOT NULL,\n" +
                        "END_TIME BIGINT(13) NULL,\n" +
                        "CALENDAR_NAME VARCHAR(190) NULL,\n" +
                        "MISFIRE_INSTR SMALLINT(2) NULL,\n" +
                        "JOB_DATA BLOB NULL,\n" +
                        "PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),\n" +
                        "FOREIGN KEY (SCHED_NAME,JOB_NAME,JOB_GROUP)\n" +
                        "REFERENCES hqframework_quartz_JOB_DETAILS(SCHED_NAME,JOB_NAME,JOB_GROUP))\n" +
                        "ENGINE=InnoDB;"
            override val createSimpleTriggersTable: String
                get() = "CREATE TABLE hqframework_quartz_SIMPLE_TRIGGERS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "TRIGGER_NAME VARCHAR(190) NOT NULL,\n" +
                        "TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                        "REPEAT_COUNT BIGINT(7) NOT NULL,\n" +
                        "REPEAT_INTERVAL BIGINT(12) NOT NULL,\n" +
                        "TIMES_TRIGGERED BIGINT(10) NOT NULL,\n" +
                        "PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),\n" +
                        "FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)\n" +
                        "REFERENCES hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP))\n" +
                        "ENGINE=InnoDB;"
            override val createCronTriggersTable: String
                get() = "CREATE TABLE hqframework_quartz_CRON_TRIGGERS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "TRIGGER_NAME VARCHAR(190) NOT NULL,\n" +
                        "TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                        "CRON_EXPRESSION VARCHAR(120) NOT NULL,\n" +
                        "TIME_ZONE_ID VARCHAR(80),\n" +
                        "PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),\n" +
                        "FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)\n" +
                        "REFERENCES hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP))\n" +
                        "ENGINE=InnoDB;"
            override val createSimpropTriggersTable: String
                get() = "CREATE TABLE hqframework_quartz_SIMPROP_TRIGGERS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "TRIGGER_NAME VARCHAR(190) NOT NULL,\n" +
                        "TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                        "STR_PROP_1 VARCHAR(512) NULL,\n" +
                        "STR_PROP_2 VARCHAR(512) NULL,\n" +
                        "STR_PROP_3 VARCHAR(512) NULL,\n" +
                        "INT_PROP_1 INT NULL,\n" +
                        "INT_PROP_2 INT NULL,\n" +
                        "LONG_PROP_1 BIGINT NULL,\n" +
                        "LONG_PROP_2 BIGINT NULL,\n" +
                        "DEC_PROP_1 NUMERIC(13,4) NULL,\n" +
                        "DEC_PROP_2 NUMERIC(13,4) NULL,\n" +
                        "BOOL_PROP_1 VARCHAR(1) NULL,\n" +
                        "BOOL_PROP_2 VARCHAR(1) NULL,\n" +
                        "PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),\n" +
                        "FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)\n" +
                        "REFERENCES hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP))\n" +
                        "ENGINE=InnoDB;\n"
            override val createBlobTriggersTable: String
                get() = "CREATE TABLE hqframework_quartz_BLOB_TRIGGERS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "TRIGGER_NAME VARCHAR(190) NOT NULL,\n" +
                        "TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                        "BLOB_DATA BLOB NULL,\n" +
                        "PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),\n" +
                        "INDEX (SCHED_NAME,TRIGGER_NAME, TRIGGER_GROUP),\n" +
                        "FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)\n" +
                        "REFERENCES hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP))\n" +
                        "ENGINE=InnoDB;"
            override val createCalendarsTable: String
                get() = "CREATE TABLE hqframework_quartz_CALENDARS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "CALENDAR_NAME VARCHAR(190) NOT NULL,\n" +
                        "CALENDAR BLOB NOT NULL,\n" +
                        "PRIMARY KEY (SCHED_NAME,CALENDAR_NAME))\n" +
                        "ENGINE=InnoDB;"
            override val createPausedTriggersGRPSTable: String
                get() = "CREATE TABLE hqframework_quartz_PAUSED_TRIGGER_GRPS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                        "PRIMARY KEY (SCHED_NAME,TRIGGER_GROUP))\n" +
                        "ENGINE=InnoDB;"
            override val createFiredTriggersTable: String
                get() = "CREATE TABLE hqframework_quartz_FIRED_TRIGGERS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "ENTRY_ID VARCHAR(95) NOT NULL,\n" +
                        "TRIGGER_NAME VARCHAR(190) NOT NULL,\n" +
                        "TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                        "INSTANCE_NAME VARCHAR(190) NOT NULL,\n" +
                        "FIRED_TIME BIGINT(13) NOT NULL,\n" +
                        "SCHED_TIME BIGINT(13) NOT NULL,\n" +
                        "PRIORITY INTEGER NOT NULL,\n" +
                        "STATE VARCHAR(16) NOT NULL,\n" +
                        "JOB_NAME VARCHAR(190) NULL,\n" +
                        "JOB_GROUP VARCHAR(190) NULL,\n" +
                        "IS_NONCONCURRENT VARCHAR(1) NULL,\n" +
                        "REQUESTS_RECOVERY VARCHAR(1) NULL,\n" +
                        "PRIMARY KEY (SCHED_NAME,ENTRY_ID))\n" +
                        "ENGINE=InnoDB;"
            override val createSchedulerStateTable: String
                get() = "CREATE TABLE hqframework_quartz_SCHEDULER_STATE (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "INSTANCE_NAME VARCHAR(190) NOT NULL,\n" +
                        "LAST_CHECKIN_TIME BIGINT(13) NOT NULL,\n" +
                        "CHECKIN_INTERVAL BIGINT(13) NOT NULL,\n" +
                        "PRIMARY KEY (SCHED_NAME,INSTANCE_NAME))\n" +
                        "ENGINE=InnoDB;"
            override val createLocksTable: String
                get() = "CREATE TABLE hqframework_quartz_LOCKS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "LOCK_NAME VARCHAR(40) NOT NULL,\n" +
                        "PRIMARY KEY (SCHED_NAME,LOCK_NAME))\n" +
                        "ENGINE=InnoDB;"
            override val indexes: List<String>
                get() = listOf(
                        "CREATE INDEX IDX_hqframework_quartz_J_REQ_RECOVERY ON hqframework_quartz_JOB_DETAILS(SCHED_NAME,REQUESTS_RECOVERY);",
                        "CREATE INDEX IDX_hqframework_quartz_J_GRP ON hqframework_quartz_JOB_DETAILS(SCHED_NAME,JOB_GROUP);",
                        "CREATE INDEX IDX_hqframework_quartz_T_J ON hqframework_quartz_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);",
                        "CREATE INDEX IDX_hqframework_quartz_T_JG ON hqframework_quartz_TRIGGERS(SCHED_NAME,JOB_GROUP);",
                        "CREATE INDEX IDX_hqframework_quartz_T_C ON hqframework_quartz_TRIGGERS(SCHED_NAME,CALENDAR_NAME);",
                        "CREATE INDEX IDX_hqframework_quartz_T_G ON hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);",
                        "CREATE INDEX IDX_hqframework_quartz_T_STATE ON hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_STATE);",
                        "CREATE INDEX IDX_hqframework_quartz_T_N_STATE ON hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_STATE);",
                        "CREATE INDEX IDX_hqframework_quartz_T_N_G_STATE ON hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_GROUP,TRIGGER_STATE);",
                        "CREATE INDEX IDX_hqframework_quartz_T_NEXT_FIRE_TIME ON hqframework_quartz_TRIGGERS(SCHED_NAME,NEXT_FIRE_TIME);",
                        "CREATE INDEX IDX_hqframework_quartz_T_NFT_ST ON hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_STATE,NEXT_FIRE_TIME);",
                        "CREATE INDEX IDX_hqframework_quartz_T_NFT_MISFIRE ON hqframework_quartz_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME);",
                        "CREATE INDEX IDX_hqframework_quartz_T_NFT_ST_MISFIRE ON hqframework_quartz_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_STATE);",
                        "CREATE INDEX IDX_hqframework_quartz_T_NFT_ST_MISFIRE_GRP ON hqframework_quartz_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_GROUP,TRIGGER_STATE);",
                        "CREATE INDEX IDX_hqframework_quartz_FT_TRIG_INST_NAME ON hqframework_quartz_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME);",
                        "CREATE INDEX IDX_hqframework_quartz_FT_INST_JOB_REQ_RCVRY ON hqframework_quartz_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME,REQUESTS_RECOVERY);",
                        "CREATE INDEX IDX_hqframework_quartz_FT_J_G ON hqframework_quartz_FIRED_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);",
                        "CREATE INDEX IDX_hqframework_quartz_FT_JG ON hqframework_quartz_FIRED_TRIGGERS(SCHED_NAME,JOB_GROUP);",
                        "CREATE INDEX IDX_hqframework_quartz_FT_T_G ON hqframework_quartz_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);",
                        "CREATE INDEX IDX_hqframework_quartz_FT_TG ON hqframework_quartz_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);"
                )
        }

        object H2DB : QuerySet {
            override val showTables: String
                get() = "SELECT TABLE_NAME " +
                        "FROM INFORMATION_SCHEMA.TABLES " +
                        "WHERE TABLE_NAME LIKE 'HQFRAMEWORK_QUARTZ%';"

            override val createJobDetailsTable: String
                get() = "CREATE TABLE hqframework_quartz_JOB_DETAILS(\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "JOB_NAME VARCHAR(190) NOT NULL,\n" +
                        "JOB_GROUP VARCHAR(190) NOT NULL,\n" +
                        "DESCRIPTION VARCHAR(250),\n" +
                        "JOB_CLASS_NAME VARCHAR(250) NOT NULL,\n" +
                        "IS_DURABLE CHAR(1) NOT NULL,\n" +
                        "IS_NONCONCURRENT CHAR(1) NOT NULL,\n" +
                        "IS_UPDATE_DATA CHAR(1) NOT NULL,\n" +
                        "REQUESTS_RECOVERY CHAR(1) NOT NULL,\n" +
                        "JOB_DATA BLOB,\n" +
                        "PRIMARY KEY (SCHED_NAME, JOB_NAME, JOB_GROUP))"

            override val createTriggersTable: String
                get() = "CREATE TABLE hqframework_quartz_TRIGGERS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "TRIGGER_NAME VARCHAR(190) NOT NULL,\n" +
                        "TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                        "JOB_NAME VARCHAR(190) NOT NULL,\n" +
                        "JOB_GROUP VARCHAR(190) NOT NULL,\n" +
                        "DESCRIPTION VARCHAR(250),\n" +
                        "NEXT_FIRE_TIME BIGINT,\n" +
                        "PREV_FIRE_TIME BIGINT,\n" +
                        "PRIORITY INTEGER,\n" +
                        "TRIGGER_STATE VARCHAR(16) NOT NULL,\n" +
                        "TRIGGER_TYPE VARCHAR(8) NOT NULL,\n" +
                        "START_TIME BIGINT NOT NULL,\n" +
                        "END_TIME BIGINT,\n" +
                        "CALENDAR_NAME VARCHAR(190),\n" +
                        "MISFIRE_INSTR SMALLINT,\n" +
                        "JOB_DATA BLOB,\n" +
                        "PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),\n" +
                        "FOREIGN KEY (SCHED_NAME, JOB_NAME, JOB_GROUP)\n" +
                        "REFERENCES hqframework_quartz_JOB_DETAILS(SCHED_NAME, JOB_NAME, JOB_GROUP))"

            override val createSimpleTriggersTable: String
                get() = "CREATE TABLE hqframework_quartz_SIMPLE_TRIGGERS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "TRIGGER_NAME VARCHAR(190) NOT NULL,\n" +
                        "TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                        "REPEAT_COUNT BIGINT NOT NULL,\n" +
                        "REPEAT_INTERVAL BIGINT NOT NULL,\n" +
                        "TIMES_TRIGGERED BIGINT NOT NULL,\n" +
                        "PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),\n" +
                        "FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)\n" +
                        "REFERENCES hqframework_quartz_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP))"

            override val createCronTriggersTable: String
                get() = "CREATE TABLE hqframework_quartz_CRON_TRIGGERS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "TRIGGER_NAME VARCHAR(190) NOT NULL,\n" +
                        "TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                        "CRON_EXPRESSION VARCHAR(120) NOT NULL,\n" +
                        "TIME_ZONE_ID VARCHAR(80),\n" +
                        "PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),\n" +
                        "FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)\n" +
                        "REFERENCES hqframework_quartz_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP))"

            override val createSimpropTriggersTable: String
                get() = "CREATE TABLE hqframework_quartz_SIMPROP_TRIGGERS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "TRIGGER_NAME VARCHAR(190) NOT NULL,\n" +
                        "TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                        "STR_PROP_1 VARCHAR(512),\n" +
                        "STR_PROP_2 VARCHAR(512),\n" +
                        "STR_PROP_3 VARCHAR(512),\n" +
                        "INT_PROP_1 INT,\n" +
                        "INT_PROP_2 INT,\n" +
                        "LONG_PROP_1 BIGINT,\n" +
                        "LONG_PROP_2 BIGINT,\n" +
                        "DEC_PROP_1 NUMERIC(13,4),\n" +
                        "DEC_PROP_2 NUMERIC(13,4),\n" +
                        "BOOL_PROP_1 CHAR(1),\n" +
                        "BOOL_PROP_2 CHAR(1),\n" +
                        "PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),\n" +
                        "FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)\n" +
                        "REFERENCES hqframework_quartz_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP))"

            override val createBlobTriggersTable: String
                get() = "CREATE TABLE hqframework_quartz_BLOB_TRIGGERS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "TRIGGER_NAME VARCHAR(190) NOT NULL,\n" +
                        "TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                        "BLOB_DATA BLOB,\n" +
                        "PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),\n" +
                        "FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)\n" +
                        "REFERENCES hqframework_quartz_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP))"

            override val createCalendarsTable: String
                get() = "CREATE TABLE hqframework_quartz_CALENDARS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "CALENDAR_NAME VARCHAR(190) NOT NULL,\n" +
                        "CALENDAR BLOB NOT NULL,\n" +
                        "PRIMARY KEY (SCHED_NAME, CALENDAR_NAME))"

            override val createPausedTriggersGRPSTable: String
                get() = "CREATE TABLE hqframework_quartz_PAUSED_TRIGGER_GRPS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                        "PRIMARY KEY (SCHED_NAME, TRIGGER_GROUP))"

            override val createFiredTriggersTable: String
                get() = "CREATE TABLE hqframework_quartz_FIRED_TRIGGERS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "ENTRY_ID VARCHAR(95) NOT NULL,\n" +
                        "TRIGGER_NAME VARCHAR(190) NOT NULL,\n" +
                        "TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                        "INSTANCE_NAME VARCHAR(190) NOT NULL,\n" +
                        "FIRED_TIME BIGINT NOT NULL,\n" +
                        "SCHED_TIME BIGINT NOT NULL,\n" +
                        "PRIORITY INTEGER NOT NULL,\n" +
                        "STATE VARCHAR(16) NOT NULL,\n" +
                        "JOB_NAME VARCHAR(190),\n" +
                        "JOB_GROUP VARCHAR(190),\n" +
                        "IS_NONCONCURRENT CHAR(1),\n" +
                        "REQUESTS_RECOVERY CHAR(1),\n" +
                        "PRIMARY KEY (SCHED_NAME, ENTRY_ID))"

            override val createSchedulerStateTable: String
                get() = "CREATE TABLE hqframework_quartz_SCHEDULER_STATE (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "INSTANCE_NAME VARCHAR(190) NOT NULL,\n" +
                        "LAST_CHECKIN_TIME BIGINT NOT NULL,\n" +
                        "CHECKIN_INTERVAL BIGINT NOT NULL,\n" +
                        "PRIMARY KEY (SCHED_NAME, INSTANCE_NAME))"

            override val createLocksTable: String
                get() = "CREATE TABLE hqframework_quartz_LOCKS (\n" +
                        "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                        "LOCK_NAME VARCHAR(40) NOT NULL,\n" +
                        "PRIMARY KEY (SCHED_NAME, LOCK_NAME))"

            override val indexes: List<String>
                get() = listOf(
                    "CREATE INDEX IDX_hqframework_quartz_J_REQ_RECOVERY ON hqframework_quartz_JOB_DETAILS(SCHED_NAME, REQUESTS_RECOVERY);",
                    "CREATE INDEX IDX_hqframework_quartz_J_GRP ON hqframework_quartz_JOB_DETAILS(SCHED_NAME, JOB_GROUP);",
                    "CREATE INDEX IDX_hqframework_quartz_T_J ON hqframework_quartz_TRIGGERS(SCHED_NAME, JOB_NAME, JOB_GROUP);",
                    "CREATE INDEX IDX_hqframework_quartz_T_JG ON hqframework_quartz_TRIGGERS(SCHED_NAME, JOB_GROUP);",
                    "CREATE INDEX IDX_hqframework_quartz_T_C ON hqframework_quartz_TRIGGERS(SCHED_NAME, CALENDAR_NAME);",
                    "CREATE INDEX IDX_hqframework_quartz_T_G ON hqframework_quartz_TRIGGERS(SCHED_NAME, TRIGGER_GROUP);",
                    "CREATE INDEX IDX_hqframework_quartz_T_STATE ON hqframework_quartz_TRIGGERS(SCHED_NAME, TRIGGER_STATE);",
                    "CREATE INDEX IDX_hqframework_quartz_T_N_STATE ON hqframework_quartz_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP, TRIGGER_STATE);",
                    "CREATE INDEX IDX_hqframework_quartz_T_N_G_STATE ON hqframework_quartz_TRIGGERS(SCHED_NAME, TRIGGER_GROUP, TRIGGER_STATE);",
                    "CREATE INDEX IDX_hqframework_quartz_T_NEXT_FIRE_TIME ON hqframework_quartz_TRIGGERS(SCHED_NAME, NEXT_FIRE_TIME);",
                    "CREATE INDEX IDX_hqframework_quartz_T_NFT_ST ON hqframework_quartz_TRIGGERS(SCHED_NAME, TRIGGER_STATE, NEXT_FIRE_TIME);",
                    "CREATE INDEX IDX_hqframework_quartz_T_NFT_MISFIRE ON hqframework_quartz_TRIGGERS(SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME);",
                    "CREATE INDEX IDX_hqframework_quartz_T_NFT_ST_MISFIRE ON hqframework_quartz_TRIGGERS(SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME, TRIGGER_STATE);",
                    "CREATE INDEX IDX_hqframework_quartz_T_NFT_ST_MISFIRE_GRP ON hqframework_quartz_TRIGGERS(SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME, TRIGGER_GROUP, TRIGGER_STATE);",
                    "CREATE INDEX IDX_hqframework_quartz_FT_TRIG_INST_NAME ON hqframework_quartz_FIRED_TRIGGERS(SCHED_NAME, INSTANCE_NAME);",
                    "CREATE INDEX IDX_hqframework_quartz_FT_INST_JOB_REQ_RCVRY ON hqframework_quartz_FIRED_TRIGGERS(SCHED_NAME, INSTANCE_NAME, REQUESTS_RECOVERY);",
                    "CREATE INDEX IDX_hqframework_quartz_FT_J_G ON hqframework_quartz_FIRED_TRIGGERS(SCHED_NAME, JOB_NAME, JOB_GROUP);",
                    "CREATE INDEX IDX_hqframework_quartz_FT_JG ON hqframework_quartz_FIRED_TRIGGERS(SCHED_NAME, JOB_GROUP);",
                    "CREATE INDEX IDX_hqframework_quartz_FT_T_G ON hqframework_quartz_FIRED_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP);",
                    "CREATE INDEX IDX_hqframework_quartz_FT_TG ON hqframework_quartz_FIRED_TRIGGERS(SCHED_NAME, TRIGGER_GROUP);"
                )
        }
    }

    @Setup
    fun setup() {
        val querySet = when (dataSource) {
            is SQLiteDataSource -> QuerySet.SQLite
            is MySQLDataSource -> QuerySet.InnoDB
            is H2DataSource -> QuerySet.H2DB
            else -> throw UnsupportedOperationException("unsupported datasource provided.")
        }
        val tablesSize = dataSource.connection.use { connection ->
            connection.prepareStatement(querySet.showTables, ResultSet.TYPE_FORWARD_ONLY).use { preparedStatement ->
                preparedStatement.executeQuery().use { resultSet ->
                    var size = 0
                    while (resultSet.next()) {
                        size++
                    }
                    size
                }
            }
        }
        if (tablesSize == 0) {
            dropIfExistsAndCreateTables(querySet)
        } else if (tablesSize != 11) {
            logger.severe("Some of the scheduler tables are missing. Dropping all tables and recreating tables.")
            dropIfExistsAndCreateTables(querySet)
        }
    }

    private fun dropIfExistsAndCreateTables(querySet: QuerySet) {
        logger.info("${AnsiColor.CYAN}Initializing Scheduler tables...")
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            fun execute(query: String) {
                connection.prepareStatement(query).use { preparedStatement ->
                    preparedStatement.execute()
                }
            }
            execute(querySet.dropFiredTriggersTable)
            execute(querySet.dropPausedTriggerGRPSTable)
            execute(querySet.dropSchedulerStateTable)
            execute(querySet.dropLocksTable)
            execute(querySet.dropSimpleTriggersTable)
            execute(querySet.dropSimpropTriggersTable)
            execute(querySet.dropCronTriggersTable)
            execute(querySet.dropBlobTriggersTable)
            execute(querySet.dropTriggersTable)
            execute(querySet.dropJobDetailsTable)
            execute(querySet.dropCalendarsTable)
            connection.commit()
            execute(querySet.createJobDetailsTable)
            execute(querySet.createTriggersTable)
            execute(querySet.createSimpleTriggersTable)
            execute(querySet.createCronTriggersTable)
            execute(querySet.createSimpropTriggersTable)
            execute(querySet.createBlobTriggersTable)
            execute(querySet.createCalendarsTable)
            execute(querySet.createPausedTriggersGRPSTable)
            execute(querySet.createFiredTriggersTable)
            execute(querySet.createSchedulerStateTable)
            execute(querySet.createLocksTable)
            querySet.indexes.forEach { execute(it) }
            connection.commit()
        }
        logger.info("${AnsiColor.CYAN}finish.")
    }
}