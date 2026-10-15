package kr.hqservice.framework.inventory.util

import com.destroystokyo.paper.profile.PlayerProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitAsync
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitMain
import kr.hqservice.framework.global.core.component.Bean
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

@Bean
class PlayerSkullRepository(
    private val coroutineScope: CoroutineScope,
) {
    private class PendingSkull(
        val itemStack: ItemStack,
        val targetedItemStack: ItemStack,
        val metaScope: (ItemMeta) -> Unit
    )

    private val skinProfileMap = ConcurrentHashMap<UUID, PlayerProfile>()
    private val lambdaQueueMap = ConcurrentHashMap<UUID, ConcurrentLinkedQueue<PendingSkull>>()

    fun setOwnerPlayer(
        targetUniqueId: UUID,
        inventory: Inventory,
        slot: Int,
        targetedItemStack: ItemStack,
        metaScope: (ItemMeta) -> Unit
    ) {
        val pendingSkull = PendingSkull(inventory.getItem(slot) ?: return, targetedItemStack, metaScope)
        var cachedProfile: PlayerProfile? = null
        var isFirstRequest = false
        lambdaQueueMap.compute(targetUniqueId) { _, queue ->
            cachedProfile = skinProfileMap[targetUniqueId]
            if (cachedProfile != null) null
            else (queue ?: ConcurrentLinkedQueue<PendingSkull>().also { isFirstRequest = true }).apply { offer(pendingSkull) }
        }

        val profile = cachedProfile
        if (profile != null) pendingSkull.applyProfile(profile)
        else if (isFirstRequest) fetchProfile(targetUniqueId)
    }

    private fun fetchProfile(targetUniqueId: UUID) {
        coroutineScope.launch(Dispatchers.BukkitAsync) {
            val profile = Bukkit.createProfile(targetUniqueId)
            var drainedQueue: ConcurrentLinkedQueue<PendingSkull>? = null
            lambdaQueueMap.compute(targetUniqueId) { _, queue ->
                skinProfileMap[targetUniqueId] = profile
                drainedQueue = queue
                null
            }
            val queue = drainedQueue ?: return@launch
            coroutineScope.launch(Dispatchers.BukkitMain) {
                queue.forEach { pendingSkull ->
                    try {
                        pendingSkull.applyProfile(profile)
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    private fun PendingSkull.applyProfile(profile: PlayerProfile) {
        if (itemStack.type.isAir || !targetedItemStack.isSimilar(itemStack)) return
        val skullMeta = itemStack.itemMeta
        if (skullMeta is SkullMeta) {
            skullMeta.playerProfile = profile
            itemStack.itemMeta = skullMeta.also(metaScope)
        }
    }
}
