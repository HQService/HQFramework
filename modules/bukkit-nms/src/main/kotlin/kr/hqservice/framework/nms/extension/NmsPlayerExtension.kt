package kr.hqservice.framework.nms.extension

import kotlinx.coroutines.launch
import kr.hqservice.framework.coroutine.component.HQCoroutineScope
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.factory.VirtualFactory
import kr.hqservice.framework.nms.virtual.factory.impl.GlobalVirtualFactory
import kr.hqservice.framework.nms.virtual.factory.impl.SingleVirtualFactory
import org.bukkit.Location
import org.bukkit.entity.Player
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent.getKoin

private val reflectionWrapper: NmsReflectionWrapper by getKoin().inject()
private val scope: HQCoroutineScope by getKoin().inject(named("virtual"))

fun Player.virtual(vararg wrapper: Virtual) {
    scope.launch {
        reflectionWrapper.sendPacket(this@virtual, *wrapper) }
}

fun Player.virtual(distance: Double = .0, factoryScope: suspend VirtualFactory.()->Unit) {
    val factory: VirtualFactory = if(distance > .0) {
        val receivers = location.world?.getNearbyEntities(location, distance, distance, distance)?.filterIsInstance<Player>()?.filter { it.isOnline }?: emptyList()
        GlobalVirtualFactory(receivers, reflectionWrapper)
    } else SingleVirtualFactory(this, reflectionWrapper)
    scope.launch {
        factory.factoryScope() }
}

fun Location.virtual(distance: Double, factoryScope: suspend VirtualFactory.() -> Unit) {
    val receivers = world?.getNearbyEntities(this, distance, distance, distance)?.filterIsInstance<Player>()?.filter { it.isOnline }?: emptyList()
    val factory = GlobalVirtualFactory(receivers, reflectionWrapper)
    scope.launch {
        factory.factoryScope() }
}