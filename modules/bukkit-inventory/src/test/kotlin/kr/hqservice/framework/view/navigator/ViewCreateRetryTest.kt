package kr.hqservice.framework.view.navigator

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ViewCreateRetryTest {
    @Test
    fun `failed onCreate is retried on the next open`() {
        val view = RecordingView(failOnCreate = true)

        assertThrows<IllegalStateException> { runBlocking { view.open { } } }
        assertThrows<IllegalStateException> { runBlocking { view.open { } } }

        assertEquals(2, view.createCount.get())
    }
}
