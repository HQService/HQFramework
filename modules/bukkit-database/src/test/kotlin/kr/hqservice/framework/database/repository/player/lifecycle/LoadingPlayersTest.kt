package kr.hqservice.framework.database.repository.player.lifecycle

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class LoadingPlayersTest {
    private val loading = LoadingPlayers()
    private val uuid = UUID.randomUUID()

    @Test
    fun `ending the first of two overlapping joins keeps the second marker`() {
        val first = loading.begin(uuid)
        val second = loading.begin(uuid)

        loading.end(uuid, first)
        assertTrue(uuid in loading)

        loading.end(uuid, second)
        assertFalse(uuid in loading)
    }

    @Test
    fun `remove clears the marker regardless of token`() {
        loading.begin(uuid)

        loading.remove(uuid)

        assertFalse(uuid in loading)
    }
}
