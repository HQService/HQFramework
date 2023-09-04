package kr.hqservice.framework.bukkit.scheduler

import kr.hqservice.framework.global.core.component.Bean
import org.quartz.SchedulerException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.logging.Level
import java.util.logging.Logger

@Bean
class HQFrameworkSchedulerDetailsSetter(private val logger: Logger) {
    fun setDetails(
        target: Any, schedulerName: String,
        schedulerId: String
    ) {
        set(target, "setInstanceName", schedulerName)
        set(target, "setInstanceId", schedulerId)
    }

    private operator fun set(target: Any, method: String, value: String) {
        val setter: Method
        try {
            setter = target.javaClass.getMethod(method, String::class.java)
        } catch (e: SecurityException) {
            logger.log(Level.SEVERE, e) {
                "A SecurityException occured: ${e.message}"
            }
            return
        } catch (e: NoSuchMethodException) {
            logger.log(Level.SEVERE, e) {
                "${target.javaClass.getName()} does not contain public method " + method + "(String)"
            }
            return
        }
        if (Modifier.isAbstract(setter.modifiers)) {
            logger.severe((target.javaClass.getName()
                        + " does not implement " + method
                        + "(String)")
            )
            return
        }
        try {
            setter.invoke(target, value)
        } catch (ite: InvocationTargetException) {
            throw SchedulerException(ite.targetException)
        } catch (e: Exception) {
            throw SchedulerException(e)
        }
    }
}