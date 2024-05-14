package kr.hqservice.framework.bukkit.core.scheduler

interface HQScheduler {
    fun runTask(runnable: suspend () -> Unit): HQTask

    fun runTaskLater(delay: Long, runnable: suspend () -> Unit): HQTask

    fun runTaskTimer(delay: Long, period: Long,  runnable: suspend () -> Unit): HQTask

    fun runTaskAsynchronously(runnable: suspend () -> Unit): HQTask

    fun runTaskLaterAsynchronously(delay: Long, runnable: suspend () -> Unit): HQTask

    fun runTaskTimerAsynchronously(delay: Long, period: Long, runnable: suspend () -> Unit): HQTask
}