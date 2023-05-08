package kr.hqservice.framework.core

import kr.hqservice.framework.core.component.registry.ComponentRegistry
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.java.JavaPluginLoader
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import java.io.File

abstract class HQPlugin : JavaPlugin, KoinComponent {
    constructor() : super()
    internal constructor(loader: JavaPluginLoader, description: PluginDescriptionFile, dataFolder: File, file: File) : super(loader, description, dataFolder, file)

    protected open val componentRegistry: ComponentRegistry by inject { parametersOf(this) }

    final override fun onEnable() {
        onPreEnable()
        componentRegistry.setup()
        onPostEnable()
    }

    final override fun onDisable() {
        onPreDisable()
        componentRegistry.teardown()
        onPostDisable()
    }

    open fun onPreEnable() {}
    open fun onPostEnable() {}
    open fun onPreDisable() {}
    open fun onPostDisable() {}

    internal fun getJar(): File {
        return super.getFile()
    }
}