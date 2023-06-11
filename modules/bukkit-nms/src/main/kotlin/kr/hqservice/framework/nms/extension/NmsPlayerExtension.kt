package kr.hqservice.framework.nms.extension

import kotlinx.coroutines.launch
import kr.hqservice.framework.coroutine.component.HQCoroutineScope
import kr.hqservice.framework.nms.util.NmsReflectionUtil
import kr.hqservice.framework.nms.wrapper.packet.NmsPacketWrapper
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent.getKoin

private val reflectionUtil: NmsReflectionUtil by getKoin().inject()
private val scope: HQCoroutineScope by getKoin().inject(named("packet"))

fun Player.sendPacket(vararg wrapper: NmsPacketWrapper) {
    scope.launch {
        reflectionUtil.sendPacket(this@sendPacket, *wrapper)
    }
}