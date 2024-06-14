package kr.hqservice.framework.bukkit.core.scheduler

interface HQTask {
    fun isCancelled(): Boolean

    fun cancel()
}