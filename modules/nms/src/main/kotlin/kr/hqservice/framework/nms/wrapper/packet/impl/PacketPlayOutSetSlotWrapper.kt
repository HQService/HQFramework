package kr.hqservice.framework.nms.wrapper.packet.impl

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.util.NmsReflectionUtil
import kr.hqservice.framework.nms.util.getStaticFunction
import kr.hqservice.framework.nms.wrapper.packet.PacketWrapper
import org.bukkit.inventory.ItemStack
import org.koin.core.annotation.InjectedParam
import kotlin.reflect.KMutableProperty
import kotlin.reflect.jvm.isAccessible

@org.koin.core.annotation.Factory(binds = [PacketPlayOutSetSlotWrapper::class])
class PacketPlayOutSetSlotWrapper(
    @InjectedParam instance: Any,
    reflectionUtil: NmsReflectionUtil
) : PacketWrapper(instance) {

    private val nmsItemStackClass = reflectionUtil.getNmsClass("ItemStack", Version.V_15.handle("world.item"))
    private val itemStack = reflectionUtil.getField(getPacketInstance()::class, nmsItemStackClass)
    private val craftItemStack = reflectionUtil.getCraftBukkitClass("CraftItemStack")

    private val asNmsCopyFunction = reflectionUtil.getStaticFunction(craftItemStack, "asNMSCopy", nmsItemStackClass, listOf(ItemStack::class))
    private val asBukkitCopyFunction = reflectionUtil.getStaticFunction(craftItemStack, "asBukkitCopy", ItemStack::class, listOf(nmsItemStackClass))

    fun getItemStack(): ItemStack {
        return asBukkitCopyFunction.call(null, itemStack) as ItemStack
    }

    fun setItemStack(itemStack: ItemStack) {
        val property = this.itemStack
        property.isAccessible = true
        if (property is KMutableProperty<*>) {
            property.setter.call(getPacketInstance(), asNmsCopyFunction.call(null, itemStack))
        }
    }

}