package kr.hqservice.framework.nms.extension

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kr.hqservice.framework.bukkit.core.coroutine.component.coroutinescope.HQCoroutineScope
import kr.hqservice.framework.nms.service.container.NmsContainerService
import kr.hqservice.framework.nms.service.item.NmsItemStackService
import kr.hqservice.framework.nms.virtual.handler.VirtualItemHandlerFactory
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import kr.hqservice.framework.nms.virtual.scope.VirtualViewScope
import kr.hqservice.framework.nms.virtual.scope.impl.GlobalVirtualScope
import kr.hqservice.framework.nms.virtual.scope.impl.SingleVirtualScope
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import org.bukkit.Location
import org.bukkit.entity.Player
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent.getKoin

private val reflectionWrapper: NmsReflectionWrapper by getKoin().inject()
private val itemStackService: NmsItemStackService by getKoin().inject()
private val containerService: NmsContainerService by getKoin().inject()
private val handlerRegistry: VirtualHandlerRegistry by getKoin().inject()
private val itemHandlerFactory: VirtualItemHandlerFactory by getKoin().inject()

private val scope: HQCoroutineScope by getKoin().inject(named("virtual"))

fun Player.virtual(distance: Double, virtualScope: suspend GlobalVirtualScope.() -> Unit): Job {
    val factory = if (distance > .0) {
        val receivers =
            location.world?.getNearbyEntities(location, distance, distance, distance)?.filterIsInstance<Player>()
                ?.filter { it.isOnline } ?: emptyList()
        GlobalVirtualScope(receivers, reflectionWrapper)
    } else throw IllegalArgumentException("distance 는 0 이하일 수 없습니다.")
    return scope.launch {
        factory.virtualScope()
    }
}

fun Player.virtual(virtualScope: suspend SingleVirtualScope.() -> Unit): Job {
    val factory = SingleVirtualScope(this, reflectionWrapper)
    return scope.launch {
        factory.virtualScope()
    }
}

fun Location.virtual(distance: Double, virtualScope: suspend GlobalVirtualScope.() -> Unit): Job {
    if (distance <= .0) throw IllegalArgumentException("distance 는 0 이하일 수 없습니다.")
    val receivers =
        world?.getNearbyEntities(this, distance, distance, distance)?.filterIsInstance<Player>()?.filter { it.isOnline }
            ?: emptyList()
    val factory = GlobalVirtualScope(receivers, reflectionWrapper)
    return scope.launch {
        factory.virtualScope()
    }
}

fun Player.virtualView(virtualScope: VirtualViewScope.() -> Unit) {
    val scope = VirtualViewScope(itemStackService, reflectionWrapper, itemHandlerFactory, containerService.wrap(this).getContainerId())
    scope.virtualScope()
    handlerRegistry.register(uniqueId, scope.create())
}