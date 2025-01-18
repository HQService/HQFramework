package kr.hqservice.framework.nms.wrapper

abstract class ContainerWrapper : NmsWrapper {
    abstract fun getContainerId(): Int

    abstract fun getStateId(): Int
}