package kr.hqservice.framework.bukkit.scheduler

import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.global.core.component.Configuration
import org.quartz.ListenerManager
import org.quartz.Scheduler
import org.quartz.SchedulerFactory
import org.quartz.impl.DefaultThreadExecutor
import org.quartz.impl.SchedulerRepository
import org.quartz.simpl.CascadingClassLoadHelper
import org.quartz.spi.ClassLoadHelper
import org.quartz.spi.ThreadExecutor

@Configuration
class HQFrameworkSchedulerConfig {
    @Bean
    fun provideListenerManager(scheduler: Scheduler): ListenerManager {
        return scheduler.listenerManager
    }

    @Bean
    fun provideSchedulerRepository(): SchedulerRepository {
        return SchedulerRepository.getInstance()
    }

    @Bean
    fun provideThreadExecutor(): ThreadExecutor {
        return DefaultThreadExecutor()
    }

    @Bean
    fun provideClassLoadingHelper(): ClassLoadHelper {
        return CascadingClassLoadHelper().also { helper ->
            helper.initialize()
        }
    }

    @Bean
    fun provideScheduler(schedulerFactory: SchedulerFactory): Scheduler {
        return schedulerFactory.scheduler
    }
}