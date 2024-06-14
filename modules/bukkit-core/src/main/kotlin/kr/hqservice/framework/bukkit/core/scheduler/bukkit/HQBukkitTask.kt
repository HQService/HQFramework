package kr.hqservice.framework.bukkit.core.scheduler.bukkit

import kr.hqservice.framework.bukkit.core.scheduler.HQTask
import org.bukkit.scheduler.BukkitTask

class HQBukkitTask(
    private val bukkitTask: BukkitTask
) : HQTask {
    override fun isCancelled(): Boolean {
        return bukkitTask.isCancelled
    }

    override fun cancel() {
        bukkitTask.cancel()
    }
}