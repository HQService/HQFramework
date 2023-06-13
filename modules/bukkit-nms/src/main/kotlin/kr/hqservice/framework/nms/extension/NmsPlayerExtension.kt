package kr.hqservice.framework.nms.extension

import kotlinx.coroutines.launch
import kr.hqservice.framework.coroutine.component.HQCoroutineScope
import kr.hqservice.framework.nms.util.NmsReflectionUtil
import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.factory.VirtualFactory
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent.getKoin

private val reflectionUtil: NmsReflectionUtil by getKoin().inject()
private val scope: HQCoroutineScope by getKoin().inject(named("virtual"))

fun Player.virtual(vararg wrapper: Virtual) {
    scope.launch {
        reflectionUtil.sendPacket(this@virtual, *wrapper)
    }
}

fun Player.virtual(factoryScope: suspend VirtualFactory.()->Unit) {
    val factory = VirtualFactory(this)
    scope.launch {
        factory.factoryScope()
            reflectionUtil.sendPacket(factory.receiver, *factory.getVirtualList().toTypedArray()) }
}