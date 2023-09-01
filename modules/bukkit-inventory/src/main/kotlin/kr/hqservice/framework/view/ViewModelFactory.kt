package kr.hqservice.framework.view

import kr.hqservice.framework.bukkit.core.component.registry.registry.BukkitPluginScopedInstanceProvider
import kr.hqservice.framework.bukkit.core.util.PluginScopeFinder
import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.global.core.component.error.NoBeanDefinitionsFoundException
import kr.hqservice.framework.global.core.component.registry.ComponentRegistry
import java.util.logging.Logger
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

@Bean
class ViewModelFactory(
    private val componentRegistry: ComponentRegistry,
    private val logger: Logger
) {
    fun <T : ViewModel> provideViewModel(viewModelClass: KClass<T>): ViewModel {
        val plugin = PluginScopeFinder.get(viewModelClass)
        val jobConstructorParameters = componentRegistry.injectParameters(viewModelClass.primaryConstructor!!, BukkitPluginScopedInstanceProvider.provideInstance(plugin))
        if (jobConstructorParameters.any { it == null }) {
            logger.severe("Injected parameters have null. Printing parameters.")
            jobConstructorParameters.forEachIndexed { index, any ->
                logger.severe("${index}: $any")
            }
            throw NoBeanDefinitionsFoundException()
        }

        return viewModelClass.primaryConstructor!!.call(*jobConstructorParameters.toTypedArray())
    }
}