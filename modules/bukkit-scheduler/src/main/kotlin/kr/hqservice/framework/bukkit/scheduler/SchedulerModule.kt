package kr.hqservice.framework.bukkit.scheduler

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.component.module.Module
import kr.hqservice.framework.bukkit.core.component.module.Setup
import org.bukkit.Server
import org.quartz.CronScheduleBuilder
import org.quartz.Job
import org.quartz.JobBuilder
import org.quartz.JobExecutionContext
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import java.util.*

@Module
class SchedulerModule(private val scheduler: Scheduler) {
    @Setup
    fun setup() {
        val jobDetail = JobBuilder
            .newJob(TestJob::class.java)
            .withIdentity(UUID.randomUUID().toString(), "testgroup")
            .build()
        val trigger = TriggerBuilder.newTrigger()
            .withSchedule(CronScheduleBuilder.cronSchedule("0/3 * * * * ?"))
            .build()
        scheduler.scheduleJob(jobDetail, trigger)
    }
}

class TestJob(private val server: Server, private val plugin: HQBukkitPlugin) : Job {
    override fun execute(context: JobExecutionContext?) {
        server.broadcastMessage("job!: ${plugin.name}")
    }
}