package kr.hqservice.framework.bukkit.core.scheduler

interface HQScheduler {
    fun runTask(runnable: () -> Unit)

    fun runTaskLater(delay: Long, runnable: () -> Unit): HQTask

    fun runTaskTimer(delay: Long, period: Long,  runnable: () -> Unit): HQTask

    fun runTaskAsynchronously(runnable: () -> Unit): HQTask

    fun runTaskLaterAsynchronously(delay: Long, runnable: () -> Unit): HQTask

    fun runTaskTimerAsynchronously(delay: Long, period: Long, runnable: () -> Unit): HQTask
}