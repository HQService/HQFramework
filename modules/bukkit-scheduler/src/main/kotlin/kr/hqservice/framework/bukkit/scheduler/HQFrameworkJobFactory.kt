package kr.hqservice.framework.bukkit.scheduler

import kr.hqservice.framework.bukkit.core.component.registry.registry.BukkitPluginScopedInstanceProvider
import kr.hqservice.framework.bukkit.core.util.PluginScopeFinder
import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.global.core.component.error.NoBeanDefinitionsFoundException
import kr.hqservice.framework.global.core.component.registry.ComponentRegistry
import org.quartz.Job
import org.quartz.JobDataMap
import org.quartz.Scheduler
import org.quartz.simpl.PropertySettingJobFactory
import org.quartz.spi.TriggerFiredBundle
import java.util.logging.Logger
import kotlin.reflect.full.primaryConstructor

@Bean
class HQFrameworkJobFactory(
    private val componentRegistry: ComponentRegistry,
    private val logger: Logger
) : PropertySettingJobFactory() {
    override fun newJob(bundle: TriggerFiredBundle, scheduler: Scheduler): Job {
        val jobDetail = bundle.jobDetail
        val jobClass = jobDetail.jobClass

        val plugin = PluginScopeFinder.get(jobClass.kotlin)
        val jobConstructorParameters = componentRegistry.injectParameters(jobClass.kotlin.primaryConstructor!!, BukkitPluginScopedInstanceProvider.provideInstance(plugin))
        if (jobConstructorParameters.any { it == null }) {
            logger.severe("Injected parameters have null. Printing parameters.")
            jobConstructorParameters.forEachIndexed { index, any ->
                logger.severe("${index}: $any")
            }
            throw NoBeanDefinitionsFoundException()
        }

        val job = jobClass.kotlin.primaryConstructor!!.call(*jobConstructorParameters.toTypedArray())
        val jobDataMap = JobDataMap()
        jobDataMap.putAll(scheduler.context)
        jobDataMap.putAll(bundle.jobDetail.jobDataMap)
        jobDataMap.putAll(bundle.trigger.jobDataMap)
        setBeanProps(job, jobDataMap)
        return job
    }
}