package kr.hqservice.framework.bukkit.core

import be.seeseemelk.mockbukkit.ServerMock
import io.mockk.mockk
import kr.hqservice.framework.bukkit.core.component.registry.registry.BukkitComponentRegistry
import kr.hqservice.framework.test.ExcludeTestSearch
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.java.JavaPluginLoader
import java.io.File

@ExcludeTestSearch
open class TestHQBukkitPlugin : HQBukkitPlugin {
    constructor() : super()
    constructor(
        loader: JavaPluginLoader,
        description: PluginDescriptionFile,
        dataFolder: File,
        file: File
    ) : super(loader, description, dataFolder, file)

    override val bukkitComponentRegistry: BukkitComponentRegistry = mockk(relaxed = true)

    companion object {
        fun load(server: ServerMock): TestHQBukkitPlugin {
            val description = PluginDescriptionFile("HQFramework", "1.0.0", TestHQBukkitPlugin::class.java.name)
            val plugin = server.pluginManager.loadPlugin(TestHQBukkitPlugin::class.java, description, emptyArray<Any>())
            server.pluginManager.enablePlugin(plugin)
            return plugin as TestHQBukkitPlugin
        }
    }
}
