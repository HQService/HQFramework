package kr.hqservice.framework.command.test

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import be.seeseemelk.mockbukkit.entity.PlayerMock
import io.mockk.mockk
import kr.hqservice.framework.command.CommandResolution
import kr.hqservice.framework.command.RegisteredCommandExecutor
import kr.hqservice.framework.command.RegisteredCommandRoot
import kr.hqservice.framework.command.RegisteredCommandTree
import kr.hqservice.framework.command.handler.CommandAnnotationHandler
import org.bukkit.command.CommandSender
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CommandPermissionTest {
    class RootCommand
    class AdminCommand
    class AdminExecutors {
        fun give(sender: CommandSender) {}
    }

    private lateinit var server: ServerMock
    private lateinit var root: RegisteredCommandRoot
    private lateinit var give: RegisteredCommandExecutor

    @BeforeEach
    fun setup() {
        server = MockBukkit.mock()
        root = RegisteredCommandRoot(RootCommand::class, "x", "", false, false, emptyList())
        val admin = RegisteredCommandTree(AdminCommand::class, "admin", emptyList(), 0, "x.admin", false, false)
        give = RegisteredCommandExecutor(
            label = "give",
            aliases = emptyList(),
            executorInstance = AdminExecutors(),
            function = AdminExecutors::give
        )
        admin.registerExecutor(give)
        root.registerTree(admin)
    }

    @AfterEach
    fun teardown() {
        MockBukkit.unmock()
    }

    private fun playerWithAdminPermission(): PlayerMock {
        return server.addPlayer().apply { addAttachment(MockBukkit.createMockPlugin(), "x.admin", true) }
    }

    @Test
    fun `sender without tree permission is denied the executor under it`() {
        val player = server.addPlayer()
        assertSame(CommandResolution.Denied, root.resolveExecutor(player, arrayOf("admin", "give")))
    }

    @Test
    fun `sender with tree permission resolves the executor under it`() {
        val resolution = root.resolveExecutor(playerWithAdminPermission(), arrayOf("admin", "give"))
        assertIs<CommandResolution.Found>(resolution)
        assertSame(give, resolution.executor)
    }

    @Test
    fun `help hides trees the sender cannot use`() {
        val player = server.addPlayer()
        assertTrue(root.getTextComponents(player).none { it.toPlainText().contains("admin") })
        assertFalse(root.getTextComponents(playerWithAdminPermission()).none { it.toPlainText().contains("admin") })
    }

    @Test
    fun `tab completion does not reveal children of a tree the sender cannot use`() {
        val command = CommandAnnotationHandler.HQBukkitCommand(
            "x", emptyList(), false, mockk(relaxed = true), root, mockk(relaxed = true), mockk(relaxed = true)
        )
        assertEquals(emptyList(), command.hqTabComplete(server.addPlayer(), "x", arrayOf("admin", ""), null))
        assertEquals(listOf("give"), command.hqTabComplete(playerWithAdminPermission(), "x", arrayOf("admin", ""), null))
    }

    @Test
    fun `op flag requires op even when the permission is granted`() {
        val opOnly = RegisteredCommandTree(AdminCommand::class, "op", emptyList(), 0, "", true, false)
        val player = server.addPlayer()
        assertFalse(opOnly.canUse(player))
        player.isOp = true
        assertTrue(opOnly.canUse(player))
    }
}
