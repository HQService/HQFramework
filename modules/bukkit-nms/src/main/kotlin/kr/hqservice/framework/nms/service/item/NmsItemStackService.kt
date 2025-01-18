package kr.hqservice.framework.nms.service.item

import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.item.NmsItemStackWrapper
import org.bukkit.inventory.ItemStack

interface NmsItemStackService : NmsService<ItemStack, NmsItemStackWrapper>