package kr.hqservice.framework.bukkit.core.extension

import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import kr.hqservice.framework.yaml.extension.yaml
import org.bukkit.plugin.Plugin
import java.io.File

fun Plugin.getHQConfig(): HQYamlConfiguration {
    return File(dataFolder, "config.yml").yaml()
}