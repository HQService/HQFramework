package kr.hqservice.framework.nms.wrapper.container

import kr.hqservice.framework.nms.wrapper.NmsWrapper

interface ContainerWrapper : NmsWrapper {
    fun getContainerId(): Int

    fun getStateId(): Int
}