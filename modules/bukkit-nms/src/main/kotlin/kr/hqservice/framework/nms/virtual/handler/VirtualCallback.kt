package kr.hqservice.framework.nms.virtual.handler

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.hqservice.framework.bukkit.core.coroutine.component.coroutinescope.HQCoroutineScope
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitMain
import org.bukkit.Bukkit
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent.getKoin
import java.util.logging.Level

private val virtualScope: HQCoroutineScope by getKoin().inject(named("virtual"))

fun launchVirtualCallback(block: suspend () -> Unit) {
    virtualScope.launch(Dispatchers.BukkitMain) {
        runCatching { block() }.onFailure { throwable ->
            if (throwable is CancellationException) throw throwable
            val logger = Bukkit.getPluginManager().getPlugin("HQFramework")?.logger ?: Bukkit.getLogger()
            logger.log(Level.SEVERE, "virtual handler callback failed", throwable)
        }
    }
}
