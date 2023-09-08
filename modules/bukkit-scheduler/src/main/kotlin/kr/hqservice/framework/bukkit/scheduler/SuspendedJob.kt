package kr.hqservice.framework.bukkit.scheduler

import org.quartz.Job
import org.quartz.JobExecutionContext

abstract class SuspendedJob : Job {
    abstract suspend fun executeSuspend(context: JobExecutionContext)

    final override fun execute(context: JobExecutionContext?) {
        throw UnsupportedOperationException()
    }
}