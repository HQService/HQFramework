package kr.hqservice.framework.bukkit.core.extension

import kr.hqservice.framework.yaml.config.HQYamlConfigurationSection
import org.bukkit.Material

fun HQYamlConfigurationSection.findMaterial(path: String): Material? {
    return getString(path).uppercase().run(Material::getMaterial)
}

fun HQYamlConfigurationSection.getMaterial(path: String): Material {
    val materialText = getString(path).uppercase()
    return Material.getMaterial(materialText) ?: throw NullPointerException("${materialText}은(는) 존재하지 않는 Material입니다.")
}

fun HQYamlConfigurationSection.findColorizedString(path: String): String? {
    return findString(path)?.colorize()
}

fun HQYamlConfigurationSection.getColorizedString(path: String): String {
    return getString(path).colorize()
}

fun HQYamlConfigurationSection.getColorizedStringList(path: String): List<String> {
    return getStringList(path).map(String::colorize)
}