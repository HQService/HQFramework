package kr.hqservice.framework.core.nms.handler.impl

import kr.hqservice.framework.core.extension.nmsEdit
import kr.hqservice.framework.core.extension.tagEdit
import kr.hqservice.framework.core.nms.Version
import kr.hqservice.framework.core.nms.handler.FunctionType
import kr.hqservice.framework.core.nms.handler.VersionHandler
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KType

class CallableVersionHandler(
    private val version: Version,
    private val functionType: FunctionType
) : VersionHandler {

    private val name: String = functionType.getName()
    private val returnType: KType? = functionType.getReturnType()
    private val params: List<KClass<*>> = functionType.getParameterClasses()

    override fun getVersion(): Version {
        ItemStack(Material.STONE)
            .nmsEdit {
                tagEdit {

                }
            }
        return version
    }

    override fun getName(): String {
        return functionType.getName()
    }

    override fun isMatched(callable: KCallable<*>): Boolean {
        return callable.name == name && callable.parameters == params && returnType?.equals(callable.returnType) != false
    }

}