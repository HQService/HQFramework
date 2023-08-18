package kr.hqservice.framework.command

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.extension.sendColorizedMessage
import kr.hqservice.framework.global.core.extension.toHumanReadable
import org.bukkit.command.CommandSender
import kotlin.coroutines.CoroutineContext

@Command(
    parent = HQFrameworkCommand::class,
    label = "plugin"
)
class PluginDescriptionCommand : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    @CommandExecutor(
        label = "version",
        description = "해당 HQPlugin 의 버전을 확인합니다.",
        isOp = true,
        priority = 2
    )
    fun sendVersion(sender: CommandSender, plugin: HQBukkitPlugin) {
        sender.sendColorizedMessage("&a버전: &7${plugin.description.version}")
    }

    @CommandExecutor(
        label = "authors",
        description = "해당 HQPlugin 의 개발자를 확인합니다.",
        isOp = true,
        priority = 3
    )
    fun sendAuthors(sender: CommandSender, plugin: HQBukkitPlugin) {
        sender.sendColorizedMessage("&a개발자: &7${plugin.description.authors.toHumanReadable()}")
    }
}