package kr.hqservice.framework.bukkit.scheduler.component

import kr.hqservice.framework.global.core.component.HQComponent
import org.quartz.JobExecutionContext
import org.quartz.Trigger
import org.quartz.TriggerListener

interface HQTriggerListener : TriggerListener, HQComponent {
    override fun getName(): String

    override fun triggerComplete(trigger: Trigger, context: JobExecutionContext, triggerInstructionCode: Trigger.CompletedExecutionInstruction)

    override fun triggerFired(trigger: Trigger, context: JobExecutionContext)

    override fun triggerMisfired(trigger: Trigger)

    override fun vetoJobExecution(trigger: Trigger, context: JobExecutionContext): Boolean
}