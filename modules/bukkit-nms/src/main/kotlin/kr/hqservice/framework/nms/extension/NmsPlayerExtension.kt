package kr.hqservice.framework.nms.extension

import kotlinx.coroutines.launch
import kr.hqservice.framework.coroutine.component.HQCoroutineScope
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.factory.VirtualFactory
import org.bukkit.entity.Player
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent.getKoin

private val reflectionWrapper: NmsReflectionWrapper by getKoin().inject()
private val scope: HQCoroutineScope by getKoin().inject(named("virtual"))

fun Player.virtual(vararg wrapper: Virtual) {
    scope.launch {
        reflectionWrapper.sendPacket(this@virtual, *wrapper)
    }
}

fun Player.virtual(factoryScope: suspend VirtualFactory.()->Unit) {
    val factory = VirtualFactory(this, reflectionWrapper)
    scope.launch {
        factory.factoryScope()
    }
}