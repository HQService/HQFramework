package kr.hqservice.framework.command

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.extension.colorize
import kr.hqservice.framework.command.component.*
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.extension.toHumanReadable
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.coroutines.CoroutineContext

@Component
@ParentCommand(binds = [HQFrameworkCommand.Plugin.Description::class])
class PluginDescriptionCommand : HQCommandNode, CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    @CommandExecutor(
        label = "version",
        description = "해당 HQPlugin 의 버전을 확인합니다.",
        isOp = true,
        priority = 2
    )
    fun sendVersion(sender: CommandSender, plugin: HQBukkitPlugin) {
        sender.sendMessage("&a버전: &7${plugin.description.version}".colorize())
    }

    @CommandExecutor(
        label = "authors",
        description = "해당 HQPlugin 의 개발자를 확인합니다.",
        isOp = true,
        priority = 3
    )
    fun sendAuthors(sender: CommandSender, plugin: HQBukkitPlugin) {
        sender.sendMessage("&a개발자: &7${plugin.description.authors.toHumanReadable()}".colorize())
    }

    @CommandExecutor(
        label = "testgreedy",
        description = "",
        isOp = true,
        priority = 999
    )
    fun testGreedyArguments(sender: CommandSender, @ArgumentLabel("문자잉") string: String, @ContextKey("intint") int: Int, plugin: HQBukkitPlugin, stringNullable: String? = "default", intNullable: Int?) {
        sender.sendMessage("$string, $int, ${plugin.name}, $stringNullable, $intNullable")
    }

    @CommandExecutor(
        label = "testgreedy11",
        description = "",
        isOp = true,
        priority = 999
    )
    suspend fun testGreedyArguments2(sender: Player, @ArgumentLabel("문자잉2") string: String, int: Int, plugin: HQBukkitPlugin) {
        sender.sendMessage("$string, $int, ${plugin.name} 1111")
        delay(3000)
        sender.sendMessage("$string, $int, ${plugin.name}")
    }
}