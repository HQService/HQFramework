package kr.hqservice.framework.nms

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class VersionResolutionTest {
    @ParameterizedTest
    @CsvSource(
        "1.21.10, V_21_8",
        "1.21.9, V_21_8",
        "1.21.2, V_21_1",
        "1.21, V_21",
        "1.20.5, ",
        "26.2, V_26_2",
        "26.3, V_26_2",
        "1.21.11, V_21_11",
        "26.1.2, V_26_1",
        "1.20.4, V_20_4",
        "1.20.6-R0.1-SNAPSHOT, V_20_6",
    )
    fun resolve(versionName: String, expected: String?) {
        assertEquals(expected?.let(Version::valueOf), Version.resolve(versionName))
    }

    @Test
    fun matches_ignores_patch_of_new_naming_and_detects_fallback() {
        assertTrue(Version.V_26_1.matches("26.1.2"))
        assertTrue(Version.V_21.matches("1.21"))
        assertFalse(Version.V_21_8.matches("1.21.10"))
    }

    @Test
    fun display_name_follows_mojang_naming() {
        assertEquals("1.21.8", Version.V_21_8.displayName)
        assertEquals("1.21", Version.V_21.displayName)
        assertEquals("26.2", Version.V_26_2.displayName)
    }
}
