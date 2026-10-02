package kr.hqservice.framework.view.navigator

import kr.hqservice.framework.view.View
import kr.hqservice.framework.view.scope.CloseScope
import kr.hqservice.framework.view.scope.CreateScope
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

class RecordingView(private val failOnCreate: Boolean = false) : View(9, "recording") {
    val createCount = AtomicInteger()
    val closedViewers: MutableList<UUID> = CopyOnWriteArrayList()
    val isDisposed: Boolean get() = lifecycleJob.isCancelled

    override suspend fun CreateScope.onCreate() {
        createCount.incrementAndGet()
        if (failOnCreate) throw IllegalStateException("onCreate failed")
    }

    override fun CloseScope.onClose(viewer: Player) {
        closedViewers.add(viewer.uniqueId)
    }
}
