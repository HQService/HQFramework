package kr.hqservice.framework.bukkit.scheduler

import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import org.bukkit.Server
import java.net.InetAddress

internal fun resolveSchedulerInstanceId(config: HQYamlConfiguration, server: Server): String {
    val configured = config.getString("scheduler.instance-id", "")
    if (configured.isNotBlank()) return configured
    val host = server.ip.ifBlank { runCatching { InetAddress.getLocalHost().hostName }.getOrDefault("localhost") }
    return "$host:${server.port}"
}
