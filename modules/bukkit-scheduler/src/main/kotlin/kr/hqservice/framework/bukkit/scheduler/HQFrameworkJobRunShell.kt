package kr.hqservice.framework.bukkit.scheduler

import org.quartz.Scheduler
import org.quartz.core.JobRunShell
import org.quartz.spi.TriggerFiredBundle

class HQFrameworkJobRunShell(
    scheduler: Scheduler,
    triggerBundle: TriggerFiredBundle
) : JobRunShell(scheduler, triggerBundle) {
    override fun run() {
        try {
            super.run()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }
}