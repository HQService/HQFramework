package kr.hqservice.framework.nms.extension

import kotlinx.coroutines.launch
import kr.hqservice.framework.coroutine.component.HQCoroutineScope
import kr.hqservice.framework.nms.util.NmsReflectionUtil
import kr.hqservice.framework.nms.wrapper.packet.PacketWrapper
import org.bukkit.entity.Player
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent.getKoin

private val reflectionUtil: NmsReflectionUtil by getKoin().inject()
private val scope: HQCoroutineScope by getKoin().inject(named("packet"))

fun Player.sendPacket(vararg wrapper: PacketWrapper) {
    scope.launch {
        reflectionUtil.sendPacket(this@sendPacket, *wrapper)
    }
}