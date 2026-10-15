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

    @Test
    fun `only the latest join token is current`() {
        val first = loading.begin(uuid)
        assertTrue(loading.isCurrent(uuid, first))

        val second = loading.begin(uuid)
        assertFalse(loading.isCurrent(uuid, first))
        assertTrue(loading.isCurrent(uuid, second))

        loading.remove(uuid)
        assertFalse(loading.isCurrent(uuid, second))
    }

    @Test
    fun `a join is superseded only by a newer join, not by a quit`() {
        val first = loading.begin(uuid)
        assertFalse(loading.isSuperseded(uuid, first))

        loading.remove(uuid)
        assertFalse(loading.isSuperseded(uuid, first))

        val second = loading.begin(uuid)
        assertTrue(loading.isSuperseded(uuid, first))
        assertFalse(loading.isSuperseded(uuid, second))
    }
}
