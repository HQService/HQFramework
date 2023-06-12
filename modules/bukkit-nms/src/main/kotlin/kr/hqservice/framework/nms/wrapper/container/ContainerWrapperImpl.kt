package kr.hqservice.framework.nms.wrapper.container

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.extension.callAccess
import kr.hqservice.framework.nms.util.NmsReflectionUtil
import kr.hqservice.framework.nms.wrapper.ContainerWrapper
import kotlin.reflect.KClass

class ContainerWrapperImpl(
    private val container: Any,
    reflectionUtil: NmsReflectionUtil,
    containerClass: KClass<*>
) : ContainerWrapper {
    private val windowIdField = reflectionUtil.getField(containerClass, "windowId",
        Version.V_17.handle("j")
    )
    private val stateIdField = reflectionUtil.getField(containerClass, "q",
        Version.V_19.handle("q"),
        Version.V_19_1.handle("r")
    )

    override fun getContainerId(): Int {
        return windowIdField.callAccess(container)
    }

    override fun getStateId(): Int {
        return stateIdField.callAccess(container) as Int
    }

    override fun getUnwrappedInstance(): Any {
        return container
    }
}