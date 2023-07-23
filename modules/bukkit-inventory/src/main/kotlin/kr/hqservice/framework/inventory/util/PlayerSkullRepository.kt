package kr.hqservice.framework.inventory.util

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import kr.hqservice.framework.coroutine.component.HQCoroutineScope
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSimpleComponent
import kr.hqservice.framework.global.core.component.Singleton
import org.bukkit.Server
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.koin.core.annotation.Named
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

@Component
@Singleton(binds = [PlayerSkullRepository::class])
class PlayerSkullRepository(
    private val server: Server,
    @Named("url-reader") private val urlReaderCoroutineScope: HQCoroutineScope
) : HQSimpleComponent {
    private val skinTagMap = mutableMapOf<UUID, String>()
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
            if (!itemStack.type.isAir && targetedItemStack.isSimilar(itemStack))
                server.unsafe.modifyItemStack(itemStack, skinTagMap[targetUniqueId]!!).apply {
                    itemMeta = itemMeta?.also(metaScope)
                }
        } else {
            if (lambdaQueueMap.containsKey(targetUniqueId)) lambdaQueueMap[targetUniqueId]?.offer(itemStack)
            else {
                val queue = ConcurrentLinkedQueue<ItemStack>()
                lambdaQueueMap[targetUniqueId] = queue
                queue.offer(itemStack)
                urlReaderCoroutineScope.launch {
                    val contents =
                        getURLContents("https://sessionserver.mojang.com/session/minecraft/profile/$targetUniqueId")
                    val jsonObject = gson.fromJson(contents, JsonObject::class.java)
                    val value = jsonObject.getAsJsonArray("properties")[0].asJsonObject["value"].asString
                    val decoded = String(Base64.getDecoder().decode(value))
                    val newObject = gson.fromJson(decoded, JsonObject::class.java)
                    val skinUrl = newObject.getAsJsonObject("textures").getAsJsonObject("SKIN")["url"].asString
                    val skin = "{\"textures\":{\"SKIN\":{\"url\":\"$skinUrl\"}}}".toByteArray()
                    val encoded = Base64.getEncoder().encode(skin)
                    val hash = Arrays.hashCode(encoded).toLong()
                    val hashAsId = UUID(hash, hash)
                    val tag = "{SkullOwner:{Id:\"$hashAsId\", Properties:{textures:[{Value:\"$value\"}]}}}"
                    skinTagMap[targetUniqueId] = tag
                    lambdaQueueMap.remove(targetUniqueId)
                    while (queue.isNotEmpty()) {
                        try {
                            val element = queue.poll()
                            if (!element.type.isAir && targetedItemStack.isSimilar(element))
                                server.unsafe.modifyItemStack(element, tag).apply {
                                    itemMeta = itemMeta?.also(metaScope)
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