package kr.hqservice.framework.inventory.util

import com.google.gson.Gson
import com.google.gson.JsonObject
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSimpleComponent
import kr.hqservice.framework.global.core.component.HQSingleton
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*

@Component
@HQSingleton(binds = [PlayerSkullRepository::class])
class PlayerSkullRepository : HQSimpleComponent {
    private val skinTagMap = mutableMapOf<UUID, String>()
    private val gson = Gson()

    fun getPlayerSkull(targetUniqueId: UUID, amount: Int): ItemStack {
        val skinTag = skinTagMap.computeIfAbsent(targetUniqueId) {
            try {
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
                "{SkullOwner:{Id:\"$hashAsId\", Properties:{textures:[{Value:\"$value\"}]}}}"
            } catch (e: Exception) {
                throw IllegalStateException("cannot found player skull $targetUniqueId", e) } }

        val baseItem = ItemStack(Material.PLAYER_HEAD, amount)
        return Bukkit.getServer().unsafe.modifyItemStack(baseItem, skinTag)
    }

    private fun getURLContents(stringUrl: String): String {
        val builder = StringBuilder()
        val url = URL(stringUrl)
        BufferedReader(InputStreamReader(url.openStream(), StandardCharsets.UTF_8)).use { br ->
            br.lines().forEach(builder::append) }
        return builder.toString()
    }
}