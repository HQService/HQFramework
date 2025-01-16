package kr.hqservice.framework.nms.virtual.classes

interface VirtualMessageConstructor<T : Any> {
    fun newInstance(vararg args: Any): T
}