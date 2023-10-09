package kr.hqservice.framework.global.core

import kr.hqservice.framework.global.core.component.registry.ComponentRegistry
import java.io.File
import java.util.logging.Logger

interface HQPlugin {
    fun onPreLoad() {}
    fun onLoad() {}
    fun onPostLoad() {}

    fun onPreEnable() {}
    fun onEnable() {}
    fun onPostEnable() {}

    fun onPreDisable() {}
    fun onDisable() {}
    fun onPostDisable() {}

    fun getJar(): File

    fun getDataFolder(): File

    fun getLogger(): Logger

    fun getPluginClassLoader(): ClassLoader

    fun getComponentRegistry(): ComponentRegistry
}