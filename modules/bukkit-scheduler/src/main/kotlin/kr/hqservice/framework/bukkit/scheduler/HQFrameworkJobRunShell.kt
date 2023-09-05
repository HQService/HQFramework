package kr.hqservice.framework.bukkit.scheduler

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.bukkit.core.util.PluginScopeFinder
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.Trigger.CompletedExecutionInstruction
import org.quartz.core.JobRunShell
import org.quartz.spi.OperableTrigger
import org.quartz.spi.TriggerFiredBundle

class HQFrameworkJobRunShell(
    scheduler: Scheduler,
    triggerBundle: TriggerFiredBundle
) : JobRunShell(scheduler, triggerBundle) {
    @OptIn(ExperimentalStdlibApi::class)
    override fun run() {
        val plugin = PluginScopeFinder.find(jec.jobDetail.jobClass.kotlin)
        if (plugin == null) {
            log.error("Job cannot executed due to the plugin of the job '${jec.jobDetail.key}' not being found.")
            return
        }
        val frameworkScheduler = qs as HQFrameworkSchedulerCore
        qs.addInternalSchedulerListener(this)
        try {
            val trigger = jec.trigger as OperableTrigger
            val jobDetail = jec.jobDetail
            do {
                var jobExEx: JobExecutionException? = null
                val job = jec.jobInstance
                try {
                    begin()
                } catch (se: SchedulerException) {
                    qs.notifySchedulerListenersError("Error executing Job (${jec.jobDetail.key}: couldn't begin execution.", se)
                    break
                }

                try {
                    if (!notifyListenersBeginning(jec)) {
                        break
                    }
                } catch (ve: VetoedException) {
                    try {
                        val instCode = trigger.executionComplete(jec, null)
                        val casted = qs as HQFrameworkSchedulerCore
                        casted.notifyJobStoreJobVetoed(trigger, jobDetail, instCode)

                        if (jec.trigger.nextFireTime == null) {
                            qs.notifySchedulerListenersFinalized(jec.trigger)
                        }
                        complete(true)
                    } catch (se: SchedulerException) {
                        qs.notifySchedulerListenersError(
                            ("Error during veto of Job ("
                                    + jec.jobDetail.key
                                    + ": couldn't finalize execution."), se
                        )
                    }
                    break
                }
                val startTime = System.currentTimeMillis()
                var endTime = startTime

                try {
                    log.debug("Calling execute on job " + jobDetail.key)

                    if (job is SuspendedJob) {
                        runBlocking {
                            plugin.launch(Dispatchers.Default) {
                                if (plugin.isEnabled) {
                                    job.executeSuspend(jec)
                                }
                            }.join()
                        }
                    } else {
                        runBlocking(plugin.coroutineContext.minusKey(CoroutineDispatcher.Key)) {
                            if (plugin.isEnabled) {
                                job.execute(jec)
                            }
                        }
                    }
                    endTime = System.currentTimeMillis()
                } catch (jee: JobExecutionException) {
                    endTime = System.currentTimeMillis()
                    jobExEx = jee
                    log.info(("Job ${jobDetail.key} threw a JobExecutionException: "), jobExEx)
                } catch (e: Throwable) {
                    endTime = System.currentTimeMillis()
                    log.error(
                        ("Job " + jobDetail.key +
                                " threw an unhandled Exception: "), e
                    )
                    val se = SchedulerException(
                        "Job threw an unhandled exception.", e
                    )
                    qs.notifySchedulerListenersError(
                        ("Job ("
                                + jec.jobDetail.key
                                + " threw an exception."), se
                    )
                    jobExEx = JobExecutionException(se, false)
                }
                jec.jobRunTime = endTime - startTime

                if (!notifyJobListenersComplete(jec, jobExEx)) {
                    break
                }
                var instCode = CompletedExecutionInstruction.NOOP

                try {
                    instCode = trigger.executionComplete(jec, jobExEx)
                } catch (e: Exception) {
                    val se = SchedulerException("Trigger threw an unhandled exception.", e)
                    qs.notifySchedulerListenersError("Please report this error to the Quartz developers.", se)
                }

                if (!notifyTriggerListenersComplete(jec, instCode)) {
                    break
                }

                if (instCode == CompletedExecutionInstruction.RE_EXECUTE_JOB) {
                    jec.incrementRefireCount()
                    try {
                        complete(false)
                    } catch (se: SchedulerException) {
                        qs.notifySchedulerListenersError(
                            ("Error executing Job ("
                                    + jec.jobDetail.key
                                    + ": couldn't finalize execution."), se
                        )
                    }
                    continue
                }
                try {
                    complete(true)
                } catch (se: SchedulerException) {
                    qs.notifySchedulerListenersError(
                        ("Error executing Job ("
                                + jec.jobDetail.key
                                + ": couldn't finalize execution."), se
                    )
                    continue

                }
                frameworkScheduler.notifyJobStoreJobComplete(trigger, jobDetail, instCode)
                break
            } while (true)
        } finally {
            qs.removeInternalSchedulerListener(this)
        }
    }

    private fun notifyListenersBeginning(jobExecutionContext: JobExecutionContext): Boolean {
        var vetoed = false

        try {
            vetoed = qs.notifyTriggerListenersFired(jobExecutionContext)
        } catch (se: SchedulerException) {
            qs.notifySchedulerListenersError(
                "Unable to notify TriggerListener(s) while firing trigger "
                        + "(Trigger and Job will NOT be fired!). trigger= "
                        + jobExecutionContext.trigger.key + " job= "
                        + jobExecutionContext.jobDetail.key, se
            )
            return false
        }
        if (vetoed) {
            try {
                qs.notifyJobListenersWasVetoed(jobExecutionContext)
            } catch (se: SchedulerException) {
                qs.notifySchedulerListenersError(
                    "Unable to notify JobListener(s) of vetoed execution " +
                            "while firing trigger (Trigger and Job will NOT be " +
                            "fired!). trigger= "
                            + jobExecutionContext.trigger.key + " job= "
                            + jobExecutionContext.jobDetail.key, se
                )
            }
            throw VetoedException()
        }

        try {
            qs.notifyJobListenersToBeExecuted(jobExecutionContext)
        } catch (se: SchedulerException) {
            qs.notifySchedulerListenersError(
                ("Unable to notify JobListener(s) of Job to be executed: "
                        + "(Job will NOT be executed!). trigger= "
                        + jobExecutionContext.trigger.key + " job= "
                        + jobExecutionContext.jobDetail.key), se
            )
            return false
        }
        return true
    }

    private fun notifyTriggerListenersComplete(
        jobExecutionContext: JobExecutionContext,
        instCode: CompletedExecutionInstruction
    ): Boolean {
        try {
            qs.notifyTriggerListenersComplete(jobExecutionContext, instCode)
        } catch (se: SchedulerException) {
            qs.notifySchedulerListenersError(
                "Unable to notify TriggerListener(s) of Job that was executed: "
                        + "(error will be ignored). trigger= "
                        + jobExecutionContext.trigger.key + " job= "
                        + jobExecutionContext.jobDetail.key, se
            )
            return false
        }
        if (jobExecutionContext.trigger.nextFireTime == null) {
            qs.notifySchedulerListenersFinalized(jobExecutionContext.trigger)
        }
        return true
    }

    private fun notifyJobListenersComplete(
        jobExecutionContext: JobExecutionContext,
        jobExecutionException: JobExecutionException?
    ): Boolean {
        try {
            qs.notifyJobListenersWasExecuted(jobExecutionContext, jobExecutionException)
        } catch (se: SchedulerException) {
            qs.notifySchedulerListenersError(
                "Unable to notify JobListener(s) of Job that was executed: "
                        + "(error will be ignored). trigger= "
                        + jobExecutionContext.trigger.key + " job= "
                        + jobExecutionContext.jobDetail.key, se
            )
            return false
        }
        return true
    }

    private class VetoedException : RuntimeException()
}