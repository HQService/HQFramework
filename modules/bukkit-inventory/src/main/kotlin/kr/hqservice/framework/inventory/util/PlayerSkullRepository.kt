package kr.hqservice.framework.inventory.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitAsync
import kr.hqservice.framework.global.core.component.Bean
import org.bukkit.Server
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.profile.PlayerProfile
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

@Bean
class PlayerSkullRepository(
    private val server: Server,
    private val coroutineScope: CoroutineScope
) {
    private val skinProfileMap = mutableMapOf<UUID, PlayerProfile>()
    private val lambdaQueueMap = mutableMapOf<UUID, ConcurrentLinkedQueue<ItemStack>>()

    fun setOwnerPlayer(
        targetUniqueId: UUID,
        inventory: Inventory,
        slot: Int,
        targetedItemStack: ItemStack,
        metaScope: (ItemMeta) -> Unit
    ) {
        val itemStack = inventory.getItem(slot) ?: return
        if (skinProfileMap.containsKey(targetUniqueId)) {
            if (!itemStack.type.isAir && targetedItemStack.isSimilar(itemStack)) {
                val skullMeta = itemStack.itemMeta
                if (skullMeta is SkullMeta) {
                    skullMeta.ownerProfile = skinProfileMap[targetUniqueId]
                    itemStack.itemMeta = skullMeta.also(metaScope)
                }
            }
        } else {
            if (lambdaQueueMap.containsKey(targetUniqueId)) lambdaQueueMap[targetUniqueId]?.offer(itemStack)
            else {
                val queue = ConcurrentLinkedQueue<ItemStack>()
                lambdaQueueMap[targetUniqueId] = queue
                queue.offer(itemStack)
                coroutineScope.launch(Dispatchers.BukkitAsync) {
                    val offlinePlayer = server.getOfflinePlayer(targetUniqueId)

                    if (offlinePlayer.isOnline) {
                        val player = offlinePlayer.player
                        if (player != null) {
                            //val tag = "{SkullOwner:{Id:\"${player.uniqueId}\", Name:\"${player.name}\"}}"
                            val profile = player.playerProfile
                            skinProfileMap[targetUniqueId] = profile
                            lambdaQueueMap.remove(targetUniqueId)
                            while (queue.isNotEmpty()) {
                                try {
                                    val element = queue.poll()
                                    if (!element.type.isAir && targetedItemStack.isSimilar(element)) {
                                        val meta = element.itemMeta
                                        if (meta is SkullMeta) {
                                            meta.ownerProfile = profile
                                            element.itemMeta = meta.also(metaScope)
                                        }
                                    }
                                } catch (_: Exception) {
                                }
                            }
                            return@launch
                        }
                    }
                }
            }
        }
    }

    private fun getURLContents(stringUrl: String): String {
        val builder = StringBuilder()
        val url = URL(stringUrl)
        BufferedReader(InputStreamReader(url.openStream(), StandardCharsets.UTF_8)).use { br ->
            br.lines().forEach(builder::append)
        }
        return builder.toString()
    }
}