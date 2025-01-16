package kr.hqservice.framework.nms.legacy.wrapper

interface ContainerWrapper : NmsWrapper {
    fun getContainerId(): Int

    fun getStateId(): Int
}