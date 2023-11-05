package kr.hqservice.framework.nms.wrapper.container

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.extension.callAccess
import kr.hqservice.framework.nms.wrapper.ContainerWrapper
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kotlin.reflect.KClass

class ContainerWrapperImpl(
    private val container: Any,
    reflectionWrapper: NmsReflectionWrapper,
    containerClass: KClass<*>
) : ContainerWrapper {

    private val containerIdField = reflectionWrapper.getField(
        containerClass, "containerId",
        Version.V_17.handle("j"),
        Version.V_17_FORGE.handle("f_38840_")
    )
    private val stateIdField = reflectionWrapper.getField(
        containerClass, "stateId",
        Version.V_17.handle("q"),
        Version.V_18_2.handle("r"),
        Version.V_17_FORGE.handle("f_182405_")
    )

    override fun getContainerId(): Int {
        return containerIdField.callAccess(container)
    }

    override fun getStateId(): Int {
        return stateIdField.callAccess(container) as Int
    }

    override fun getUnwrappedInstance(): Any {
        return container
    }
}