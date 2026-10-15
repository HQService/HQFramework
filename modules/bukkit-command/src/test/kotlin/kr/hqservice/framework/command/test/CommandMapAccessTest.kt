package kr.hqservice.framework.command.test

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kr.hqservice.framework.command.handler.CommandMapAccess
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.command.CommandMap
import org.bukkit.command.SimpleCommandMap
import org.bukkit.plugin.SimplePluginManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

class CommandMapAccessTest {
    private lateinit var server: ServerMock

    @BeforeEach
    fun setup() {
        server = MockBukkit.mock()
    }

    @AfterEach
    fun teardown() {
        MockBukkit.unmock()
    }

    @Test
    fun `server command map is used when the paper api is present`() {
        val commandMap = mockk<CommandMap>()
        val paperServer = mockk<Server>()
        every { paperServer.commandMap } returns commandMap

        assertSame(commandMap, CommandMapAccess.commandMap(paperServer))
    }

    @Test
    fun `command map falls back to the plugin manager field on spigot`() {
        val commandMap = SimpleCommandMap(server)
        val spigotServer = mockk<Server>()
        every { spigotServer.commandMap } throws NoSuchMethodError()
        every { spigotServer.pluginManager } returns SimplePluginManager(server, commandMap)

        assertSame(commandMap, CommandMapAccess.commandMap(spigotServer))
    }

    @Test
    fun `command map is null when neither access path works`() {
        val spigotServer = mockk<Server>()
        every { spigotServer.commandMap } throws NoSuchMethodError()
        every { spigotServer.pluginManager } returns mockk()

        assertNull(CommandMapAccess.commandMap(spigotServer))
    }

    @Test
    fun `known commands fall back to the simple command map field on spigot`() {
        val commandMap = SimpleCommandMap(server)
        val knownCommands: MutableMap<String, Command> = commandMap.knownCommands
        val spigotCommandMap = spyk(commandMap)
        every { spigotCommandMap.knownCommands } throws NoSuchMethodError()

        assertSame(knownCommands, CommandMapAccess.knownCommands(spigotCommandMap))
    }
}
