package kr.hqservice.framework.nms.virtual.classes

import kr.hqservice.framework.nms.virtual.AbstractVirtualEntity

class VirtualMessageConstructor(
    private val function: (AbstractVirtualEntity) -> Any
) {
    fun newInstance(virtualEntity: AbstractVirtualEntity): Any {
        return function.invoke(virtualEntity)
    }
}