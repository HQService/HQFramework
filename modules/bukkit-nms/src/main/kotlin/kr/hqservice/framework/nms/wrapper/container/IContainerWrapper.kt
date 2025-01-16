package kr.hqservice.framework.nms.wrapper.container

import kr.hqservice.framework.nms.wrapper.ContainerWrapper

interface IContainerWrapper : ContainerWrapper {
    override fun getContainerId(): Int

    override fun getStateId(): Int

    override fun getUnwrappedInstance(): Any
}