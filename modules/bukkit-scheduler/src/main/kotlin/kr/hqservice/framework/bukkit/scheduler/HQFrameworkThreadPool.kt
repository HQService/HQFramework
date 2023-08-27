package kr.hqservice.framework.bukkit.scheduler

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import org.quartz.simpl.SimpleThreadPool
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Bean
class HQFrameworkThreadPool(
    plugin: HQBukkitPlugin,
    config: HQYamlConfiguration
) : SimpleThreadPool(
    config.getInt("scheduler.thread-pool.thread-count", 10),
    config.getInt("scheduler.thread-pool.thread-priority", Thread.NORM_PRIORITY)
) {
    init {
        isThreadsInheritContextClassLoaderOfInitializingThread = true
        isThreadsInheritGroupOfInitializingThread = true
        Thread.currentThread().contextClassLoader = plugin.getPluginClassLoader()
        super.initialize()
    }

    override fun getLog(): Logger {
        return LoggerFactory.getLogger("HQFrameworkThreadPool")
    }
}