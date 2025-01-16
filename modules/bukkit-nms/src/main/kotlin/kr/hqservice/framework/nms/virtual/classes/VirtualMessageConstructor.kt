package kr.hqservice.framework.nms.virtual.classes

interface VirtualMessageConstructor {
    fun newInstance(vararg args: Any): Any
}