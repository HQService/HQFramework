package kr.hqservice.framework.nms.extension

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kr.hqservice.framework.coroutine.component.HQCoroutineScope
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.virtual.factory.VirtualFactory
import kr.hqservice.framework.nms.virtual.factory.VirtualViewFactory
import kr.hqservice.framework.nms.virtual.factory.impl.GlobalVirtualFactory
import kr.hqservice.framework.nms.virtual.factory.impl.SingleVirtualFactory
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import kr.hqservice.framework.nms.wrapper.ContainerWrapper
import kr.hqservice.framework.nms.wrapper.item.NmsItemStackWrapper
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent.getKoin

private val reflectionWrapper: NmsReflectionWrapper by getKoin().inject()
private val itemStackService: NmsService<ItemStack, NmsItemStackWrapper> by getKoin().inject(named("itemStack"))
private val containerService: NmsService<Player, ContainerWrapper> by getKoin().inject(named("container"))
private val handlerRegistry: VirtualHandlerRegistry by getKoin().inject()
private val scope: HQCoroutineScope by getKoin().inject(named("virtual"))

suspend fun Player.virtualScope(factoryScope: suspend VirtualFactory.() -> Unit) {
    val factory: VirtualFactory = SingleVirtualFactory(this, reflectionWrapper, itemStackService)
    factory.factoryScope()
}

fun Player.virtual(distance: Double = .0, factoryScope: suspend VirtualFactory.()->Unit): Job {
    val factory: VirtualFactory = if(distance > .0) {
        val receivers = location.world?.getNearbyEntities(location, distance, distance, distance)?.filterIsInstance<Player>()?.filter { it.isOnline }?: emptyList()
        GlobalVirtualFactory(receivers, reflectionWrapper)
    } else SingleVirtualFactory(this, reflectionWrapper, itemStackService)
    return scope.launch {
        factory.factoryScope() }
}

fun Location.virtual(distance: Double, factoryScope: suspend VirtualFactory.() -> Unit): Job {
    val receivers = world?.getNearbyEntities(this, distance, distance, distance)?.filterIsInstance<Player>()?.filter { it.isOnline }?: emptyList()
    val factory = GlobalVirtualFactory(receivers, reflectionWrapper)
    return scope.launch {
        factory.factoryScope()
    }
}

fun Player.virtualView(viewFactoryScope: VirtualViewFactory.() -> Unit) {
    val scope = VirtualViewFactory(itemStackService, reflectionWrapper, containerService.wrap(this).getContainerId())
    scope.viewFactoryScope()
    handlerRegistry.register(uniqueId, scope.create())
}