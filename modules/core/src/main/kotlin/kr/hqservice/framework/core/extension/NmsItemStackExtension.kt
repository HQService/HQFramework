package kr.hqservice.framework.core.extension

import kr.hqservice.framework.core.nms.service.impl.NmsItemStackService
import kr.hqservice.framework.core.nms.service.impl.NmsNBTTagCompoundService
import kr.hqservice.framework.core.nms.wrapper.impl.NmsItemStackWrapper
import kr.hqservice.framework.core.nms.wrapper.impl.NmsNBTTagCompoundWrapper
import org.bukkit.inventory.ItemStack
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent.getKoin

fun ItemStack.nmsEdit(block: NmsItemStackWrapper.() -> Unit): ItemStack {
    val service: NmsItemStackService by getKoin().inject(named("item"))
    itemMeta = service.wrap(this).apply(block).run(service::unwrap).itemMeta
    return this
}

fun NmsItemStackWrapper.tagEdit(block: NmsNBTTagCompoundWrapper.() -> Unit) {
    val service: NmsNBTTagCompoundService by getKoin().inject(named("tag"))
    setTag(service.wrap(getTag()).apply(block))
}