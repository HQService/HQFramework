package kr.hqservice.framework.nms.wrapper

interface ContainerWrapper : NmsWrapper {
    fun getContainerId(): Int

    fun getStateId(): Int
}