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
@ParentCommand(binds = [HQFrameworkCommand.Plugin::class])
class PluginReloadCommand(private val server: Server) : HQCommandNode {
    @CommandExecutor(
        label = "hardreload",
        description = "&f/hq plugin hardreload &8<HQPlugin> &6| &7HQPlugin 을 의존하는 플러그인을 PluginManager 에서 reload 합니다.",
        isOp = true,
        priority = 1
    )
    fun executeHardReload(sender: CommandSender, plugin: HQBukkitPlugin) {
        server.pluginManager.disablePlugin(plugin)
        server.pluginManager.enablePlugin(plugin)
    }
    @CommandExecutor(
        label = "lightreload",
        description = "&f/hq plugin lightreload &8<HQPlugin> &6| &7HQPlugin 을 의존하는 플러그인을 HQPlugin 에서 reload 합니다.",
        isOp = true,
        priority = 2
    )
    fun executeLightReload(sender: CommandSender, plugin: HQBukkitPlugin) {
        if (plugin is HQFrameworkBukkitPlugin) {
            sender.sendMessage("&cFramework 는 경량 리로드가 지원되지 않습니다.".colorize())
            return
        }
        plugin.reload()
    }
}