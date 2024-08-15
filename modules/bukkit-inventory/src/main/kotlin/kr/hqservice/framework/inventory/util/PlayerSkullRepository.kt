package kr.hqservice.framework.inventory.util

import com.destroystokyo.paper.profile.PlayerProfile
import com.google.gson.Gson
import com.google.gson.JsonObject
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
    private val skinTagMap = mutableMapOf<UUID, PlayerProfile>()
    private val lambdaQueueMap = mutableMapOf<UUID, ConcurrentLinkedQueue<ItemStack>>()
    private val gson = Gson()

    fun setOwnerPlayer(
        targetUniqueId: UUID,
        inventory: Inventory,
        slot: Int,
        targetedItemStack: ItemStack,
        metaScope: (ItemMeta) -> Unit
    ) {
        val itemStack = inventory.getItem(slot) ?: return
        if (skinTagMap.containsKey(targetUniqueId)) {

            if (!itemStack.type.isAir && targetedItemStack.isSimilar(itemStack)) {
                val skullMeta = itemStack.itemMeta as SkullMeta
                // set Player Profile
                skullMeta.playerProfile = skinTagMap[targetUniqueId]
                skullMeta.also(metaScope)

                itemStack.apply {
                    itemMeta = skullMeta
                }
            }
        } else {
            if (lambdaQueueMap.containsKey(targetUniqueId)) lambdaQueueMap[targetUniqueId]?.offer(itemStack)
            else {
                val queue = ConcurrentLinkedQueue<ItemStack>()
                lambdaQueueMap[targetUniqueId] = queue
                queue.offer(itemStack)
                coroutineScope.launch(Dispatchers.BukkitAsync) {
                    val profile = server.getOfflinePlayer(targetUniqueId).playerProfile
                    skinTagMap[targetUniqueId] = profile
                    lambdaQueueMap.remove(targetUniqueId)
                    while (queue.isNotEmpty()) {
                        try {
                            val element = queue.poll()
                            if (!element.type.isAir && targetedItemStack.isSimilar(element)) {
                                val skullMeta = element.itemMeta as SkullMeta
                                skullMeta.playerProfile = profile
                                skullMeta.also(metaScope)

                                element.apply {
                                    itemMeta = skullMeta
                                }
                            }
                        } catch (_: Exception) {
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