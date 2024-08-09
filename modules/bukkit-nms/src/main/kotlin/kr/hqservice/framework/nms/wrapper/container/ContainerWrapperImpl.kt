package kr.hqservice.framework.nms.wrapper.container

import kr.hqservice.framework.nms.wrapper.ContainerWrapper
import net.minecraft.world.inventory.AbstractContainerMenu

class ContainerWrapperImpl(
    private val container: AbstractContainerMenu
) : ContainerWrapper {
    override fun getContainerId(): Int {
        return container.containerId
    }

    override fun getStateId(): Int {
        return container.stateId
    }

    override fun getUnwrappedInstance(): Any {
        return container
    }
}