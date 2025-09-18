package kr.hqservice.framework.nms.virtual.handler

interface VirtualHandler {
    fun getNmsSimpleNames(): List<String>

    fun checkCondition(message: Any): Boolean

    fun unregisterType(): HandlerUnregisterType

    fun unregisterCondition(message: Any): Boolean

    fun handle(message: Any)

    fun cancelParent(message: Any): Any? = null
}