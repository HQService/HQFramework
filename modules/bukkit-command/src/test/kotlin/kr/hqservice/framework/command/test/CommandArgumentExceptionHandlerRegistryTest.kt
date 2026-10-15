package kr.hqservice.framework.command.test

import be.seeseemelk.mockbukkit.entity.PlayerMock
import kr.hqservice.framework.command.CommandArgumentExceptionHandler
import kr.hqservice.framework.command.CommandContext
import kr.hqservice.framework.command.registry.impl.CommandArgumentExceptionHandlerRegistryImpl
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.junit.jupiter.api.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

class CommandArgumentExceptionHandlerRegistryTest {
    private class NoopHandler : CommandArgumentExceptionHandler<Throwable, CommandSender> {
        override fun handle(exception: Throwable, sender: CommandSender, context: CommandContext, argument: String?) {}
    }

    @Test
    fun `handler for a supertype exception and sender interface matches subclasses`() {
        val registry = CommandArgumentExceptionHandlerRegistryImpl()
        val handler = NoopHandler()
        registry.register(IllegalArgumentException::class, Player::class, handler)
        assertSame(handler, registry.find(NumberFormatException::class, PlayerMock::class))
    }

    @Test
    fun `most specific exception handler wins`() {
        val registry = CommandArgumentExceptionHandlerRegistryImpl()
        val generic = NoopHandler()
        val specific = NoopHandler()
        registry.register(Throwable::class, CommandSender::class, generic)
        registry.register(IllegalArgumentException::class, Player::class, specific)
        assertSame(specific, registry.find(NumberFormatException::class, PlayerMock::class))
        assertSame(generic, registry.find(IllegalStateException::class, PlayerMock::class))
    }

    @Test
    fun `unrelated exception finds nothing`() {
        val registry = CommandArgumentExceptionHandlerRegistryImpl()
        registry.register(IllegalArgumentException::class, Player::class, NoopHandler())
        assertNull(registry.find(IllegalStateException::class, PlayerMock::class))
    }
}
