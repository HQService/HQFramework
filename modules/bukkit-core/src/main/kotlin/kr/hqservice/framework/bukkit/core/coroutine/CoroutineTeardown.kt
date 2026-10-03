package kr.hqservice.framework.bukkit.core.coroutine

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kr.hqservice.framework.bukkit.core.coroutine.extension.coroutineContext
import kr.hqservice.framework.global.core.util.AnsiColor
import java.util.logging.Logger

object CoroutineTeardown {
    private const val GRACE_PERIOD_MS = 5000L
    private const val FORCE_CANCEL_TIMEOUT_MS = 2000L

    suspend fun awaitChildren(job: Job, logger: Logger, gracePeriodMs: Long = GRACE_PERIOD_MS, forceCancelTimeoutMs: Long = FORCE_CANCEL_TIMEOUT_MS) {
        val children = job.children.toList()
        if (children.isEmpty()) return
        logger.info("${AnsiColor.CYAN}Cleaning up ${children.size} coroutine(s)...${AnsiColor.RESET}")

        withTimeoutOrNull(gracePeriodMs) {
            coroutineScope {
                children.forEach { child -> launch { child.join() } }
            }
        }

        val stillActive = children.filter { !it.isCompleted }
        if (stillActive.isNotEmpty()) {
            stillActive.forEach { it.cancel() }
            withTimeoutOrNull(forceCancelTimeoutMs) {
                coroutineScope {
                    stillActive.forEach { child -> launch { child.join() } }
                }
            }

            val abandoned = children.filter { !it.isCompleted }
            abandoned.forEach { child ->
                val name = child.coroutineContext[CoroutineName]?.name
                logger.warning("${AnsiColor.CYAN}Abandoning [$name routine] - non-cancellable blocking work; server shutdown will proceed${AnsiColor.RESET}")
            }
        }

        val finishedCount = children.count { it.isCompleted }
        logger.info("${AnsiColor.CYAN}Cleaned up $finishedCount/${children.size} coroutine(s)${AnsiColor.RESET}")
    }
}
