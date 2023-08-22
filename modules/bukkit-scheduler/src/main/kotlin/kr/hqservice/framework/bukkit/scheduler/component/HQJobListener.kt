package kr.hqservice.framework.bukkit.scheduler.component

import kr.hqservice.framework.global.core.component.HQComponent
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.JobListener

interface HQJobListener : JobListener, HQComponent {
    override fun getName(): String

    override fun jobExecutionVetoed(context: JobExecutionContext)

    override fun jobToBeExecuted(context: JobExecutionContext)

    override fun jobWasExecuted(context: JobExecutionContext, jobException: JobExecutionException?)
}