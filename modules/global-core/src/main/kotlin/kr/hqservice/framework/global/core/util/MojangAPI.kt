package kr.hqservice.framework.global.core.util

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.*

object MojangAPI {

    fun findPlayerNameByUniqueId(uniqueId: UUID): String? {
        return runCatching {
            val url = URL("https://api.mojang.com/user/profile/$uniqueId")
            val inputStream = url.openStream()
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val jsonObject = JsonParser.parseReader(bufferedReader) as JsonObject
            jsonObject["name"].asString
        }.getOrNull()
    }

    fun findPlayerUniqueIdByName(name: String): UUID? {
        return runCatching {
            val url = URL("https://api.mojang.com/users/profiles/minecraft/$name")
            val inputStream = url.openStream()
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val jsonObject = JsonParser.parseReader(bufferedReader) as JsonObject
            val uniqueIdText = jsonObject["id"].asString
            val formattedUniqueIdText = String.format(
                "%s-%s-%s-%s-%s",
                uniqueIdText.substring(0, 8),
                uniqueIdText.substring(8, 12),
                uniqueIdText.substring(12, 16),
                uniqueIdText.substring(16, 20),
                uniqueIdText.substring(20)
            )
            UUID.fromString(formattedUniqueIdText)
        }.getOrNull()
    }
}