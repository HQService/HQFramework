package kr.hqservice.framework.bungee

import kr.hqservice.framework.global.core.util.MojangAPI
import java.util.UUID
import kotlin.test.Test

class MojangAPITest {

    @Test
    fun mojang_api_test() {
        val uniqueId = MojangAPI.findPlayerUniqueIdByName("Cosine_A")
        println("UUID: $uniqueId")

        val uniqueId2 = MojangAPI.findPlayerUniqueIdByName("Cosine_B")
        println("UUID2: $uniqueId2")

        val name = MojangAPI.findPlayerNameByUniqueId(UUID.fromString("f0e6d8a9-74d0-4a8d-8479-3f72fefe21bc"))
        println("Name: $name")

        val name2 = MojangAPI.findPlayerNameByUniqueId(UUID.fromString("f0e6d8a9-74d0-4a8d-8479-3f72fefe21bd"))
        println("Name2: $name2")
    }
}