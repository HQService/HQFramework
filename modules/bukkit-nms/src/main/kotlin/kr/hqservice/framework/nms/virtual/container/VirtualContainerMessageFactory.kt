package kr.hqservice.framework.nms.virtual.container

import kr.hqservice.framework.nms.virtual.VirtualMessage

interface VirtualContainerMessageFactory {
    fun create(virtualContainer: VirtualContainer): VirtualMessage?
}