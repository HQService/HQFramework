package kr.hqservice.framework.bukkit.scheduler

import kr.hqservice.framework.global.core.component.Bean
import org.quartz.Scheduler
import org.quartz.core.JobRunShell
import org.quartz.core.JobRunShellFactory
import org.quartz.spi.TriggerFiredBundle

@Bean
class HQFrameworkJobRunShellFactory : JobRunShellFactory {
    private lateinit var scheduler: Scheduler

    override fun initialize(scheduler: Scheduler) {
        this.scheduler = scheduler
    }

    override fun createJobRunShell(bundle: TriggerFiredBundle): JobRunShell {
        return HQFrameworkJobRunShell(scheduler, bundle)
    }
}