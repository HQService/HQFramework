package kr.hqservice.framework.bukkit.core.scheduler.folia

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import kr.hqservice.framework.bukkit.core.scheduler.HQTask

class HQFoliaTask(
    private val task: ScheduledTask
) : HQTask {
    override fun isCancelled(): Boolean {
        return task.isCancelled
    }

    override fun cancel() {
        task.cancel()
    }
}