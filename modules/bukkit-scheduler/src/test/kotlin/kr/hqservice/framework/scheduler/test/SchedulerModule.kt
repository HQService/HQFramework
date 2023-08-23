package kr.hqservice.framework.scheduler.test

import be.seeseemelk.mockbukkit.MockBukkit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kr.hqservice.framework.bukkit.scheduler.SchedulerConfig
import kr.hqservice.framework.database.datasource.MySQLDataSource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.quartz.*
import org.quartz.impl.matchers.GroupMatcher
import java.util.*
import javax.sql.DataSource

@OptIn(ExperimentalCoroutinesApi::class)
class SchedulerModule {
    private val testDataSource = MySQLDataSource("hqservice.kr", 3306, "hq", "test", "testpassword@")
    lateinit var schedulerFactory: SchedulerFactory

    @BeforeEach
    fun setup() {
        MockBukkit.mock()
        startKoin {
            val defaultModule = module {
                single<DataSource> { testDataSource }
            }
            loadKoinModules(defaultModule)
        }
        schedulerFactory = SchedulerConfig(testDataSource).provideSchedulerFactory()

        schedulerFactory.scheduler.getTriggerKeys(GroupMatcher.anyGroup())
            .forEach { triggerKey ->
                schedulerFactory.scheduler.unscheduleJob(triggerKey)
            }
    }

    @AfterEach
    fun teardown() {
        stopKoin()
        MockBukkit.unmock()
    }

    @Test
    fun test() = runTest {
        val jobDetail = JobBuilder
            .newJob(TestJob::class.java)
            .withIdentity("test-${UUID.randomUUID()}", "testgroup")
            .build()
        val trigger = TriggerBuilder.newTrigger()
            .withSchedule(CronScheduleBuilder.cronSchedule("*/3 * * * * ?"))
            .withIdentity("testTriggerIdentity-${UUID.randomUUID()}")
            .build()

        schedulerFactory.scheduler.scheduleJob(jobDetail, trigger)
    }

    @DisallowConcurrentExecution
    class TestJob : Job {
        override fun execute(context: JobExecutionContext?) {
            println("good job!")
        }
    }
}