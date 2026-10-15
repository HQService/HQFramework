package kr.hqservice.framework.bukkit.core.coroutine.component.exceptionhandler

import kr.hqservice.framework.bukkit.core.coroutine.component.exceptionhandler.handler.CoroutineScopeAdviceAnnotationHandler
import org.junit.jupiter.api.Test
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CoroutineScopeAdviceMatchTest {
    @Test
    fun declared_type_matches_itself() {
        assertTrue(CoroutineScopeAdviceAnnotationHandler.matches(IOException::class, IOException::class))
    }

    @Test
    fun declared_supertype_matches_subclass() {
        assertTrue(CoroutineScopeAdviceAnnotationHandler.matches(IOException::class, FileNotFoundException::class))
    }

    @Test
    fun declared_subclass_does_not_match_supertype() {
        assertFalse(CoroutineScopeAdviceAnnotationHandler.matches(FileNotFoundException::class, IOException::class))
    }

    @Test
    fun unrelated_type_does_not_match() {
        assertFalse(CoroutineScopeAdviceAnnotationHandler.matches(IOException::class, IllegalStateException::class))
    }
}
