package kr.hqservice.framework.nms.service.container

import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Service
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.ContainerWrapper
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.container.ContainerWrapperImpl
import org.bukkit.entity.Player
import kotlin.reflect.KClass

@Qualifier("container")
@Service
class NmsContainerService(
    private val reflectionWrapper: NmsReflectionWrapper
) : NmsService<Player, ContainerWrapper> {

    private val containerClass = reflectionWrapper.getNmsClass("Container",
        Version.V_17.handle("world.inventory.Container", true)
    )
    private val containerMenuField = reflectionWrapper.getField(reflectionWrapper.getNmsPlayerClass(), "containerMenu",
        Version.V_17.handle("bV"),
        Version.V_18.handle("bW"),
        Version.V_18_2.handle("bV"),
        Version.V_19.handle("bU"),
        Version.V_19_4.handle("bP"),
        Version.V_20.handle("bR"),
        Version.V_20_2.handle("bS"),
        Version.V_17_FORGE.handle("f_36096_")
    )

    override fun wrap(target: Player): ContainerWrapper {
        val nmsPlayer = reflectionWrapper.getEntityPlayer(target)
        val containerMenu = containerMenuField.call(nmsPlayer) ?: throw UnsupportedOperationException()
        return ContainerWrapperImpl(containerMenu, reflectionWrapper, containerClass)
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