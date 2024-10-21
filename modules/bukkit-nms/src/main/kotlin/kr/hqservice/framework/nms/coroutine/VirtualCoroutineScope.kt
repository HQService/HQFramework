package kr.hqservice.framework.nms.coroutine

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.coroutine.component.coroutinescope.HQCoroutineScope
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Singleton

@Qualifier("virtual")
@Singleton(binds = [HQCoroutineScope::class])
@Component
class VirtualCoroutineScope(
    plugin: HQBukkitPlugin
) : HQCoroutineScope(plugin, Dispatchers.IO) {
    private val coroutineName = CoroutineName("PacketCoroutineContext")

    override fun getCoroutineName(): CoroutineName {
        return coroutineName
    }
}