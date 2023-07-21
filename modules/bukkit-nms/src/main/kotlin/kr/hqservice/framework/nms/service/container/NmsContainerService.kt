package kr.hqservice.framework.nms.service.container

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.ContainerWrapper
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.container.ContainerWrapperImpl
import org.bukkit.entity.Player
import org.koin.core.annotation.Named
import kotlin.reflect.KClass

@Component
@Named("container")
@HQSingleton(binds = [NmsService::class])
class NmsContainerService(
    private val reflectionWrapper: NmsReflectionWrapper
) : NmsService<Player, ContainerWrapper> {
    private val containerClass =
        reflectionWrapper.getNmsClass("Container", Version.V_17.handle("world.inventory.Container", true))
    private val activeContainerField = reflectionWrapper.getField(
        reflectionWrapper.getNmsPlayerClass(), "activeContainer",
        Version.V_15.handle("bx"),
        Version.V_17.handle("bV"),
        Version.V_18.handle("bW"),
        Version.V_19.handle("bU"),
        Version.V_19_4.handle("bP")
    )

    override fun wrap(target: Player): ContainerWrapper {
        val nmsPlayer = reflectionWrapper.getEntityPlayer(target)
        val activeContainer = activeContainerField.call(nmsPlayer) ?: throw UnsupportedOperationException()
        return ContainerWrapperImpl(activeContainer, reflectionWrapper, containerClass)
    }

    override fun getWrapper(nmsInstance: Any): ContainerWrapper {
        return ContainerWrapperImpl(nmsInstance, reflectionWrapper, containerClass)
    }

    override fun unwrap(wrapper: ContainerWrapper): Player {
        throw UnsupportedOperationException()
    }

    override fun getOriginalClass(): KClass<*> {
        return Player::class
    }

    override fun getTargetClass(): KClass<*> {
        return containerClass
    }
}