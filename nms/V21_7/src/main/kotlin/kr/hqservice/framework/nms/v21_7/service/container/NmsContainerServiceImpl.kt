package kr.hqservice.framework.nms.v21_7.service.container

import kr.hqservice.framework.nms.service.container.NmsContainerService
import kr.hqservice.framework.nms.v21.wrapper.container.ContainerWrapperImpl
import kr.hqservice.framework.nms.v21_7.wrapper.reflect.NmsReflectionWrapperImpl
import kr.hqservice.framework.nms.wrapper.ContainerWrapper
import net.minecraft.world.inventory.AbstractContainerMenu
import org.bukkit.entity.Player
import kotlin.reflect.KClass

class NmsContainerServiceImpl(
    private val reflectionWrapper: NmsReflectionWrapperImpl
) : NmsContainerService {
    override fun wrap(target: Player): ContainerWrapper {
        val nmsPlayer = reflectionWrapper.getEntityPlayer(target)
        val containerMenu = nmsPlayer.containerMenu
        return ContainerWrapperImpl(containerMenu)
    }

    override fun getWrapper(nmsInstance: Any): ContainerWrapper {
        return ContainerWrapperImpl(nmsInstance as AbstractContainerMenu)
    }

    override fun unwrap(wrapper: ContainerWrapper): Player {
        throw UnsupportedOperationException()
    }

    override fun getOriginalClass(): KClass<*> {
        return Player::class
    }

    override fun getTargetClass(): KClass<*> {
        return AbstractContainerMenu::class
    }
}