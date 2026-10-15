package kr.hqservice.framework.database.repository.player

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.Duration

class SavePolicyTest {
    @Test
    fun `periodic accepts defaults and positive values`() {
        assertDoesNotThrow { SavePolicy.periodic() }
        assertDoesNotThrow { SavePolicy.periodic(Duration.ofSeconds(1), Duration.ofSeconds(10), 5) }
    }

    @Test
    fun `periodic rejects non positive dirty interval`() {
        assertThrows<IllegalArgumentException> { SavePolicy.periodic(dirtyInterval = Duration.ZERO) }
    }

    @Test
    fun `periodic rejects non positive full interval`() {
        assertThrows<IllegalArgumentException> { SavePolicy.periodic(fullInterval = Duration.ofSeconds(-1)) }
    }

    @Test
    fun `periodic rejects non positive batch size`() {
        assertThrows<IllegalArgumentException> { SavePolicy.periodic(batchSize = 0) }
    }
}
