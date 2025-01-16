package kr.hqservice.framework.nms.v20_6.wrapper.container

import kr.hqservice.framework.nms.wrapper.container.IContainerWrapper
import net.minecraft.world.inventory.AbstractContainerMenu

class ContainerWrapperImpl(
    private val container: AbstractContainerMenu
) : IContainerWrapper() {
    override fun getContainerId(): Int {
        return container.containerId
    }

    override fun getStateId(): Int {
        return container.stateId
    }

    override fun getUnwrappedInstance(): AbstractContainerMenu {
        return container
    }
}