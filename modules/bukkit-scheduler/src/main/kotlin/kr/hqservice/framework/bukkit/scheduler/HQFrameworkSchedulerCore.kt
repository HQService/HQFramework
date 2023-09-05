package kr.hqservice.framework.bukkit.scheduler

import kr.hqservice.framework.global.core.util.AnsiColor
import org.quartz.JobDetail
import org.quartz.Trigger
import org.quartz.core.QuartzScheduler
import org.quartz.core.QuartzSchedulerResources
import org.quartz.spi.JobFactory
import org.quartz.spi.OperableTrigger
import java.util.logging.Logger

class HQFrameworkSchedulerCore(
    quartzSchedulerResources: QuartzSchedulerResources,
    private val jobFactory: JobFactory,
    private val logger: Logger
) : QuartzScheduler(quartzSchedulerResources, IDLE_WAIT_TIME, DB_FAILURE_RETRY_INTERVAL) {
    companion object {
        const val IDLE_WAIT_TIME = -1L
        const val DB_FAILURE_RETRY_INTERVAL = 15000L
    }

    override fun initialize() {
        logger.info("${AnsiColor.CYAN}Scheduler '$schedulerName' with instance id $schedulerInstanceId starting...")
        logger.info("${AnsiColor.CYAN}Using thread pool '${threadPoolClass.simpleName}' - with $threadPoolSize threads.")
        logger.info(
            "${AnsiColor.CYAN}Using job-store '${jobStoreClass.simpleName} - which ${
                if (supportsPersistence()) {
                    "supports persistence."
                } else {
                    "does not support persistence."
                }
            } ${
                if (isClustered) {
                    "and is clustered."
                } else {
                    "and is not clustered."
                }
            }"
        )
    }

    override fun getJobFactory(): JobFactory {
        return jobFactory
    }

    override fun setJobFactory(factory: JobFactory) {
        throw UnsupportedOperationException("Setting job factory with runtime is not allowed.")
    }

    public override fun notifyJobStoreJobVetoed(
        trigger: OperableTrigger?,
        detail: JobDetail?,
        instCode: Trigger.CompletedExecutionInstruction?
    ) {
        super.notifyJobStoreJobVetoed(trigger, detail, instCode)
    }

    public override fun notifyJobStoreJobComplete(
        trigger: OperableTrigger?,
        detail: JobDetail?,
        instCode: Trigger.CompletedExecutionInstruction?
    ) {
        super.notifyJobStoreJobComplete(trigger, detail, instCode)
    }
}