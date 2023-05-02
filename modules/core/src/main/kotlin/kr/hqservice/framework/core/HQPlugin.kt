package kr.hqservice.framework.core

import kr.hqservice.framework.core.component.registry.ComponentRegistry
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import java.io.File

abstract class HQPlugin : JavaPlugin(), KoinComponent {
    private val componentRegistry: ComponentRegistry by inject { parametersOf(this) }

    final override fun onEnable() {
        componentRegistry.setup()
    }

    final override fun onDisable() {
        componentRegistry.teardown()
    }

    internal fun getJar(): File {
        return super.getFile()
    }
}