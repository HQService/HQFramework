package kr.hqservice.framework.command

import kr.hqservice.framework.bukkit.HQFrameworkBukkitPlugin
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.extension.colorize
import kr.hqservice.framework.command.component.CommandExecutor
import kr.hqservice.framework.command.component.HQCommandNode
import kr.hqservice.framework.command.component.ParentCommand
import kr.hqservice.framework.global.core.component.Component
import org.bukkit.Server
import org.bukkit.command.CommandSender

@Component
@ParentCommand(binds = [HQFrameworkCommand::class])
class PluginListCommand(private val server: Server) : HQCommandNode {
    @CommandExecutor(
        label = "plugins",
        description = "&7HQPlugin 을 의존하는 플러그인들을 출력합니다.",
        isOp = true,
        priority = 100
    )
    fun sendPluginList(target: CommandSender) {
        val hqPlugins = server.pluginManager.plugins.filterIsInstance<HQBukkitPlugin>()
        target.sendMessage("&aHQ Plugins (${hqPlugins.size}):".colorize())
        hqPlugins.forEach { plugin ->
            if (plugin is HQFrameworkBukkitPlugin) {
                target.sendMessage(" &7- &a${plugin.name} §8(Framework)".colorize())
            } else {
                target.sendMessage(" &7- &a${plugin.name}".colorize())
            }
        }
    }
}