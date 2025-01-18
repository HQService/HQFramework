package kr.hqservice.framework.nms.virtual.handler

import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper

interface VirtualSignHandlerFactory {
    fun createHandler(
        reflectionWrapper: NmsReflectionWrapper,
        confirmHandler: suspend (List<String>) -> Boolean
    ): VirtualHandler
}