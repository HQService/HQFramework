package kr.hqservice.framework.command

import kr.hqservice.framework.bukkit.HQFrameworkBukkitPlugin
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.extension.colorize
import org.bukkit.Server
import org.bukkit.command.CommandSender

@Command(parent = HQFrameworkCommand::class)
class PluginListCommand(
    private val bukkitPlugin: HQBukkitPlugin,
    private val server: Server
) {
    @CommandExecutor(
        label = "plugins",
        description = "HQPlugin 을 의존하는 플러그인들을 출력합니다.",
        isOp = true,
        priority = 100
    )
    fun sendPluginList(target: CommandSender) {
        val hqPlugins =
            server.pluginManager.plugins.filterIsInstance<HQBukkitPlugin>().groupBy { it.group }.toMutableMap()
        target.sendMessage("<s:b0fc90>All HQ Plugins (${hqPlugins.values.sumOf { it.size }}):".colorize())
        hqPlugins.remove("HQPlugin")?.apply {
            target.sendMessage(" <s:74b1f7>HQPlugin (${size}):".colorize())
            forEach { plugin ->
                if (plugin is HQFrameworkBukkitPlugin) {
                    target.sendMessage(" &7- <s:74b1f7>${plugin.name} &8(Framework)".colorize())
                } else {
                    target.sendMessage(" &7- <s:74b1f7>${plugin.name}".colorize())
                }
            }
        }
        printPluginGroups(target, hqPlugins)
    }

    private fun printPluginGroups(target: CommandSender, map: Map<String, List<HQBukkitPlugin>>) {
        map.forEach {
            target.sendMessage(" <s:c496ff>${it.key} (${it.value.size})".colorize())
            it.value.forEach { plugin ->
                if (plugin is HQFrameworkBukkitPlugin) {
                    target.sendMessage(" &7- <s:c496ff>${plugin.name} &8(Framework)".colorize())
                } else {
                    target.sendMessage(" &7- <s:c496ff>${plugin.name}".colorize())
                }
            }
        }
    }
}