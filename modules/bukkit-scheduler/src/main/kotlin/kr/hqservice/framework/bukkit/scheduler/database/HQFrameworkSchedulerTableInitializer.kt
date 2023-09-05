package kr.hqservice.framework.bukkit.scheduler.database

import kr.hqservice.framework.bukkit.core.component.module.Module
import kr.hqservice.framework.bukkit.core.component.module.Setup
import java.sql.ResultSet
import java.util.logging.Logger
import javax.sql.DataSource

@Module
class HQFrameworkSchedulerTableInitializer(private val dataSource: DataSource, private val logger: Logger) {
    @Suppress("SqlDialectInspection", "SqlNoDataSourceInspection")
    private companion object DDL {
        const val SHOW_TABLES = "SHOW TABLES LIKE 'hqframework_quartz%'"
        const val INITIALIZE_TABLES =
            "DROP TABLE IF EXISTS hqframework_quartz_FIRED_TRIGGERS;\n" +
                    "DROP TABLE IF EXISTS hqframework_quartz_PAUSED_TRIGGER_GRPS;\n" +
                    "DROP TABLE IF EXISTS hqframework_quartz_SCHEDULER_STATE;\n" +
                    "DROP TABLE IF EXISTS hqframework_quartz_LOCKS;\n" +
                    "DROP TABLE IF EXISTS hqframework_quartz_SIMPLE_TRIGGERS;\n" +
                    "DROP TABLE IF EXISTS hqframework_quartz_SIMPROP_TRIGGERS;\n" +
                    "DROP TABLE IF EXISTS hqframework_quartz_CRON_TRIGGERS;\n" +
                    "DROP TABLE IF EXISTS hqframework_quartz_BLOB_TRIGGERS;\n" +
                    "DROP TABLE IF EXISTS hqframework_quartz_TRIGGERS;\n" +
                    "DROP TABLE IF EXISTS hqframework_quartz_JOB_DETAILS;\n" +
                    "DROP TABLE IF EXISTS hqframework_quartz_CALENDARS;\n" +
                    "\n" +
                    "CREATE TABLE hqframework_quartz_JOB_DETAILS(\n" +
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
                    "ENGINE=InnoDB;\n" +
                    "\n" +
                    "CREATE TABLE hqframework_quartz_TRIGGERS (\n" +
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
                    "ENGINE=InnoDB;\n" +
                    "\n" +
                    "CREATE TABLE hqframework_quartz_SIMPLE_TRIGGERS (\n" +
                    "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                    "TRIGGER_NAME VARCHAR(190) NOT NULL,\n" +
                    "TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                    "REPEAT_COUNT BIGINT(7) NOT NULL,\n" +
                    "REPEAT_INTERVAL BIGINT(12) NOT NULL,\n" +
                    "TIMES_TRIGGERED BIGINT(10) NOT NULL,\n" +
                    "PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),\n" +
                    "FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)\n" +
                    "REFERENCES hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP))\n" +
                    "ENGINE=InnoDB;\n" +
                    "\n" +
                    "CREATE TABLE hqframework_quartz_CRON_TRIGGERS (\n" +
                    "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                    "TRIGGER_NAME VARCHAR(190) NOT NULL,\n" +
                    "TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                    "CRON_EXPRESSION VARCHAR(120) NOT NULL,\n" +
                    "TIME_ZONE_ID VARCHAR(80),\n" +
                    "PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),\n" +
                    "FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)\n" +
                    "REFERENCES hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP))\n" +
                    "ENGINE=InnoDB;\n" +
                    "\n" +
                    "CREATE TABLE hqframework_quartz_SIMPROP_TRIGGERS\n" +
                    "  (\n" +
                    "    SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                    "    TRIGGER_NAME VARCHAR(190) NOT NULL,\n" +
                    "    TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                    "    STR_PROP_1 VARCHAR(512) NULL,\n" +
                    "    STR_PROP_2 VARCHAR(512) NULL,\n" +
                    "    STR_PROP_3 VARCHAR(512) NULL,\n" +
                    "    INT_PROP_1 INT NULL,\n" +
                    "    INT_PROP_2 INT NULL,\n" +
                    "    LONG_PROP_1 BIGINT NULL,\n" +
                    "    LONG_PROP_2 BIGINT NULL,\n" +
                    "    DEC_PROP_1 NUMERIC(13,4) NULL,\n" +
                    "    DEC_PROP_2 NUMERIC(13,4) NULL,\n" +
                    "    BOOL_PROP_1 VARCHAR(1) NULL,\n" +
                    "    BOOL_PROP_2 VARCHAR(1) NULL,\n" +
                    "    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),\n" +
                    "    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)\n" +
                    "    REFERENCES hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP))\n" +
                    "ENGINE=InnoDB;\n" +
                    "\n" +
                    "CREATE TABLE hqframework_quartz_BLOB_TRIGGERS (\n" +
                    "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                    "TRIGGER_NAME VARCHAR(190) NOT NULL,\n" +
                    "TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                    "BLOB_DATA BLOB NULL,\n" +
                    "PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),\n" +
                    "INDEX (SCHED_NAME,TRIGGER_NAME, TRIGGER_GROUP),\n" +
                    "FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)\n" +
                    "REFERENCES hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP))\n" +
                    "ENGINE=InnoDB;\n" +
                    "\n" +
                    "CREATE TABLE hqframework_quartz_CALENDARS (\n" +
                    "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                    "CALENDAR_NAME VARCHAR(190) NOT NULL,\n" +
                    "CALENDAR BLOB NOT NULL,\n" +
                    "PRIMARY KEY (SCHED_NAME,CALENDAR_NAME))\n" +
                    "ENGINE=InnoDB;\n" +
                    "\n" +
                    "CREATE TABLE hqframework_quartz_PAUSED_TRIGGER_GRPS (\n" +
                    "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                    "TRIGGER_GROUP VARCHAR(190) NOT NULL,\n" +
                    "PRIMARY KEY (SCHED_NAME,TRIGGER_GROUP))\n" +
                    "ENGINE=InnoDB;\n" +
                    "\n" +
                    "CREATE TABLE hqframework_quartz_FIRED_TRIGGERS (\n" +
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
                    "ENGINE=InnoDB;\n" +
                    "\n" +
                    "CREATE TABLE hqframework_quartz_SCHEDULER_STATE (\n" +
                    "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                    "INSTANCE_NAME VARCHAR(190) NOT NULL,\n" +
                    "LAST_CHECKIN_TIME BIGINT(13) NOT NULL,\n" +
                    "CHECKIN_INTERVAL BIGINT(13) NOT NULL,\n" +
                    "PRIMARY KEY (SCHED_NAME,INSTANCE_NAME))\n" +
                    "ENGINE=InnoDB;\n" +
                    "\n" +
                    "CREATE TABLE hqframework_quartz_LOCKS (\n" +
                    "SCHED_NAME VARCHAR(120) NOT NULL,\n" +
                    "LOCK_NAME VARCHAR(40) NOT NULL,\n" +
                    "PRIMARY KEY (SCHED_NAME,LOCK_NAME))\n" +
                    "ENGINE=InnoDB;\n" +
                    "\n" +
                    "CREATE INDEX IDX_hqframework_quartz_J_REQ_RECOVERY ON hqframework_quartz_JOB_DETAILS(SCHED_NAME,REQUESTS_RECOVERY);\n" +
                    "CREATE INDEX IDX_hqframework_quartz_J_GRP ON hqframework_quartz_JOB_DETAILS(SCHED_NAME,JOB_GROUP);\n" +
                    "\n" +
                    "CREATE INDEX IDX_hqframework_quartz_T_J ON hqframework_quartz_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);\n" +
                    "CREATE INDEX IDX_hqframework_quartz_T_JG ON hqframework_quartz_TRIGGERS(SCHED_NAME,JOB_GROUP);\n" +
                    "CREATE INDEX IDX_hqframework_quartz_T_C ON hqframework_quartz_TRIGGERS(SCHED_NAME,CALENDAR_NAME);\n" +
                    "CREATE INDEX IDX_hqframework_quartz_T_G ON hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);\n" +
                    "CREATE INDEX IDX_hqframework_quartz_T_STATE ON hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_STATE);\n" +
                    "CREATE INDEX IDX_hqframework_quartz_T_N_STATE ON hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_STATE);\n" +
                    "CREATE INDEX IDX_hqframework_quartz_T_N_G_STATE ON hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_GROUP,TRIGGER_STATE);\n" +
                    "CREATE INDEX IDX_hqframework_quartz_T_NEXT_FIRE_TIME ON hqframework_quartz_TRIGGERS(SCHED_NAME,NEXT_FIRE_TIME);\n" +
                    "CREATE INDEX IDX_hqframework_quartz_T_NFT_ST ON hqframework_quartz_TRIGGERS(SCHED_NAME,TRIGGER_STATE,NEXT_FIRE_TIME);\n" +
                    "CREATE INDEX IDX_hqframework_quartz_T_NFT_MISFIRE ON hqframework_quartz_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME);\n" +
                    "CREATE INDEX IDX_hqframework_quartz_T_NFT_ST_MISFIRE ON hqframework_quartz_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_STATE);\n" +
                    "CREATE INDEX IDX_hqframework_quartz_T_NFT_ST_MISFIRE_GRP ON hqframework_quartz_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_GROUP,TRIGGER_STATE);\n" +
                    "\n" +
                    "CREATE INDEX IDX_hqframework_quartz_FT_TRIG_INST_NAME ON hqframework_quartz_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME);\n" +
                    "CREATE INDEX IDX_hqframework_quartz_FT_INST_JOB_REQ_RCVRY ON hqframework_quartz_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME,REQUESTS_RECOVERY);\n" +
                    "CREATE INDEX IDX_hqframework_quartz_FT_J_G ON hqframework_quartz_FIRED_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);\n" +
                    "CREATE INDEX IDX_hqframework_quartz_FT_JG ON hqframework_quartz_FIRED_TRIGGERS(SCHED_NAME,JOB_GROUP);\n" +
                    "CREATE INDEX IDX_hqframework_quartz_FT_T_G ON hqframework_quartz_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);\n" +
                    "CREATE INDEX IDX_hqframework_quartz_FT_TG ON hqframework_quartz_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);\n" +
                    "\n" +
                    "commit;\n"
    }

    @Setup
    fun setup() {
        val tablesSize = dataSource.connection.use { connection ->
            connection.prepareStatement(SHOW_TABLES, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).use { preparedStatement ->
                preparedStatement.executeQuery().use { resultSet ->
                    resultSet.last()
                    resultSet.row
                }
            }
        }
        if (tablesSize == 0) {
            dropIfExistsAndCreateTables()
        } else if (tablesSize != 11) {
            logger.severe("Some of the scheduler tables are missing. Dropping all tables and recreating tables.")
            dropIfExistsAndCreateTables()
        }
    }

    private fun dropIfExistsAndCreateTables() {
        dataSource.connection.use { connection ->
            connection.prepareStatement(INITIALIZE_TABLES).use { preparedStatement ->
                preparedStatement.execute()
            }
        }
    }
}