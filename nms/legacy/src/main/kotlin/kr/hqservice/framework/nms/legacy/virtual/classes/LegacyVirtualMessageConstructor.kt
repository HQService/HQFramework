package kr.hqservice.framework.nms.legacy.virtual.classes

import kr.hqservice.framework.nms.virtual.classes.VirtualMessageConstructor
import java.lang.reflect.Constructor

class LegacyVirtualMessageConstructor(
    private val constructor: Constructor<out Any>
) : VirtualMessageConstructor<Any> {
    override fun newInstance(vararg args: Any): Any {
        return constructor.newInstance(*args)
    }
}