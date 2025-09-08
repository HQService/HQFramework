package kr.hqservice.framework.nms.virtual.scope

import kr.hqservice.framework.nms.extension.nms
import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.container.VirtualAnvilContainer
import kr.hqservice.framework.nms.virtual.container.VirtualContainer
import kr.hqservice.framework.nms.virtual.container.VirtualPaperAnvilContainer
import kr.hqservice.framework.nms.virtual.item.VirtualItem
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class VirtualAnvilContainerScope {
    companion object {
        private val defaultBaseItems: (Player) -> List<VirtualItem> = {
            listOf(
                ItemStack(Material.BARRIER).run {
                    nms { tag { setString("BASE1_RANDOM_ID", UUID.randomUUID().toString()) } }
                    VirtualItem(it, 0, this)
                },
                ItemStack(Material.BARRIER).run {
                    nms { tag { setString("BASE1_RANDOM_ID", UUID.randomUUID().toString()) } }
                    VirtualItem(it, 1, this)
                }
            )
        }

        private val defaultResultItem: (Player) -> VirtualItem = {
            ItemStack(Material.BARRIER).run {
                nms { tag { setString("RESULT_RANDOM_ID", UUID.randomUUID().toString()) } }
                VirtualItem(it, 2, this)
            }
        }
    }

    private val receiver: Player
    private var anvilPacket: VirtualContainer
    private var baseItem: List<VirtualItem>? = null
    private var resultItem: VirtualItem? = null
    private var virtualResultSlotHandler: ((String) -> ItemStack)? = null
    private var virtualConfirmHandler: ((String) -> Boolean)? = null
    private var virtualButtonHandler: ((Int, String) -> Boolean)? = null
    private var closeHandler: ((String) -> Unit)? = null

    constructor(player: Player, title: BaseComponent) {
        receiver = player
        anvilPacket = VirtualAnvilContainer(receiver, title)
    }

    constructor(player: Player, title: Component) {
        receiver = player
        anvilPacket = VirtualPaperAnvilContainer(receiver, title)
    }



    fun setBaseItem(itemStack: ItemStack?) {
        baseItem = if (itemStack == null || itemStack.type.isAir) {
            null
        } else {
            listOf(
                itemStack.clone().run {
                    editMeta { nms { tag { setString("BASE1_RANDOM_ID", UUID.randomUUID().toString()) } } }
                    VirtualItem(receiver, 0, this)
                },
                itemStack.clone().run {
                    editMeta { nms { tag { setString("BASE2_RANDOM_ID", UUID.randomUUID().toString()) } } }
                    VirtualItem(receiver, 1, this)
                }
            )
        }
    }

    fun setBaseItems(items: List<ItemStack>?) {
        baseItem = if (items.isNullOrEmpty()) {
            null
        } else {
            items.mapIndexed { index, itemStack ->
                itemStack.clone().run {
                    editMeta { nms { tag { setString("BASE${index + 1}_RANDOM_ID", UUID.randomUUID().toString()) } } }
                    VirtualItem(receiver, index, this)
                }
            }
        }
    }

    fun setResultItem(itemStack: ItemStack?) {
        resultItem =
            if (itemStack == null || itemStack.type.isAir) null
            else itemStack.clone().run {
                nms { tag { setString("RESULT_RANDOM_ID", UUID.randomUUID().toString()) } }
                VirtualItem(receiver, 2, this)
            }
    }

    fun setInputHandler(inputHandleScope: (String) -> ItemStack) {
        virtualResultSlotHandler = inputHandleScope
    }

    fun setButtonHandler(buttonHandlerScope: (Int, String) -> Boolean) {
        virtualButtonHandler = buttonHandlerScope
    }

    fun setConfirmHandler(confirmHandleScope: (String) -> Boolean) {
        virtualConfirmHandler = confirmHandleScope
    }

    fun setCloseHandler(closeScope: (String) -> Unit) {
        closeHandler = closeScope
    }

    internal fun getBaseItem(): List<VirtualItem> {
        return baseItem ?: defaultBaseItems(receiver)
    }

    internal fun getResultItem(text: String): VirtualItem {
        val itemStack = virtualResultSlotHandler?.invoke(text) ?: return resultItem ?: return defaultResultItem(receiver)
        return VirtualItem(receiver, 2, itemStack)
    }

    internal fun confirm(text: String): Boolean {
        return virtualConfirmHandler?.invoke(text) == true
    }

    internal fun button(index: Int, text: String): Boolean {
        return virtualButtonHandler?.invoke(index, text) == true
    }

    internal fun close(text: String) {
        closeHandler?.invoke(text)
    }

    fun getMessages(): Array<Virtual> {
        val messages = mutableListOf<Virtual>()
        anvilPacket.apply(messages::add)
        messages.addAll(getBaseItem())
        return messages.toTypedArray()
    }
}