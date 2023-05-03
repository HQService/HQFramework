package kr.hqservice.framework.core.component.registry.impl

import kr.hqservice.framework.core.HQPlugin
import kr.hqservice.framework.core.component.*
import kr.hqservice.framework.core.component.error.ConstructorConflictException
import kr.hqservice.framework.core.component.handler.ComponentHandler
import kr.hqservice.framework.core.component.error.NoBeanDefinitionsFoundException
import kr.hqservice.framework.core.component.registry.ComponentRegistry
import kr.hqservice.framework.core.extension.print
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.plugin.Plugin
import org.koin.core.annotation.*
import org.koin.core.component.KoinComponent
import org.koin.core.definition.BeanDefinition
import org.koin.core.definition.Kind
import org.koin.core.definition.indexKey
import org.koin.core.error.NoBeanDefFoundException
import org.koin.core.instance.FactoryInstanceFactory
import org.koin.core.instance.InstanceContext
import org.koin.core.module.Module
import org.koin.core.instance.SingleInstanceFactory
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.TypeQualifier
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.jar.JarFile
import java.util.logging.Logger
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

@OptIn(KoinInternalApi::class)
@Single(binds = [ComponentRegistry::class])
class ComponentRegistryImpl(private val plugin: HQPlugin) : ComponentRegistry, KoinComponent {
    private val componentInstances: ComponentInstanceList = ComponentInstanceList()

    override fun setup() {
        val componentClasses = getAllPluginClasses().filter { clazz ->
            clazz.annotations.filterIsInstance<Component>().isNotEmpty()
        }
        val queue: ConcurrentLinkedQueue<KClass<*>> = ConcurrentLinkedQueue(componentClasses.map { it.kotlin })

        // 사이즈가 같은 채로 그 큐의 사이즈만큼 반복됐다면, 더 이상 definition 이 없는것으로 판단 후 throw
        var exceptionCatchingStack = 0
        var previousQueueSize = queue.size
        while (queue.isNotEmpty()) {
            val component = queue.poll()
            if (component.primaryConstructor == null) {
                val instance = component.createInstance()
                componentInstances.addSafely(instance)
                tryCreateBeanModule(component, instance)
            } else if (component.constructors.size > 1) {
                throw ConstructorConflictException(component)
            } else {
                val constructor = component.constructors.first()
                val provideInstance: Map<KClass<*>, *> = mapOf(
                    Plugin::class to plugin as Plugin,
                    plugin::class to plugin,
                    Logger::class to plugin.logger,
                    ConfigurationSection::class to plugin.config
                )
                val instance = callByInjectedParameters(constructor, provideInstance)
                if (instance == null) {
                    queue.offer(component)
                    if (previousQueueSize == queue.size) {
                        exceptionCatchingStack++
                    }
                    if (exceptionCatchingStack == queue.size) {
                        throw NoBeanDefinitionsFoundException(queue.toList())
                    }
                    previousQueueSize = queue.size
                } else {
                    componentInstances.addSafely(instance)
                    tryCreateBeanModule(component, instance)
                }
            }
        }

        componentInstances.forEach { component ->
            getComponentHandler(component::class).setup(component, plugin)
        }
    }

    override fun teardown() {
        componentInstances.forEach { component ->
            getComponentHandler(component::class).teardown(component, plugin)
        }
    }

    /**
     * @return 만약에 파라미터에 맞는 값을 찾을 수 없을 경우, null 을 리턴합니다.
     */
    private fun <T : Any> callByInjectedParameters(
        kFunction: KFunction<T>,
        providedInstanceMap: Map<KClass<*>, *>? = null
    ): T? {
        val injectedParameters = injectParameters(kFunction, providedInstanceMap)
        if (injectedParameters.any { it == null }) {
            return null
        }
        return kFunction.call(*injectedParameters.toTypedArray())
    }

    private fun injectParameters(kFunction: KFunction<*>, providedInstanceMap: Map<KClass<*>, *>? = null): List<Any?> {
        return kFunction.parameters.map { parameter ->
            val parameterKClass = parameter.type.classifier as KClass<*>
            if (providedInstanceMap != null) {
                val providedInstance = providedInstanceMap.filter { parameterKClass.isSubclassOf(it.key) }.values.firstOrNull()
                if (providedInstance != null) {
                    return@map providedInstance
                }
            }

            val qualifier = getQualifier(parameter)
            val scopeQualifier = getScopeQualifier(kFunction)
            val indexKey = indexKey(parameterKClass, qualifier, scopeQualifier)

            val factory = getKoin().instanceRegistry.instances[indexKey]
            val defaultContext = InstanceContext(getKoin(), getKoin().getScope(scopeQualifier.value))
            factory?.get(defaultContext)
        }.toList()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : HQComponent> getComponentHandler(component: KClass<T>): ComponentHandler<HQComponent> {
        return getKoin().getAll<ComponentHandler<*>>().firstOrNull { componentHandler ->
            val handlerType = componentHandler::class
                .supertypes
                .first()
                .arguments
                .first()
                .type?.jvmErasure ?: throw NullPointerException("ComponentHandlers must have one type parameter.")

            component.allSuperclasses.contains(handlerType)
        } as? ComponentHandler<HQComponent>
            ?: throw NullPointerException("ComponentHandler implementation not found for ${component.simpleName}")
    }

    private fun getAllPluginClasses(): Collection<Class<*>> {
        val classes: MutableSet<Class<*>> = mutableSetOf()
        val entries = JarFile(plugin.getJar()).entries()
        while (entries.hasMoreElements()) {
            val name = entries.nextElement().name.replace("/", ".")
            if (name.startsWith(plugin::class.java.packageName) && name.endsWith(".class")) {
                try {
                    val clazz = Class.forName(name.removeSuffix(".class"))
                    classes.add(clazz)
                } catch (exception: ClassNotFoundException) {
                    exception.printStackTrace()
                }
            }
        }
        return classes
    }

    /**
     * 모듈을 생성합니다. 만약 아무런 타입의 Bean 도 아닐 경우, 생성되지 않습니다.
     */
    private fun <T : Any> tryCreateBeanModule(klass: KClass<T>, instance: Any) {
        val scopeQualifier = getScopeQualifier(klass)
        val qualifier = getQualifier(klass)
        val bean = getBeanProperties(klass) ?: return

        val module = when (bean.first) {
            Kind.Factory -> createFactoryBeanModule(klass, instance, scopeQualifier, qualifier, bean.second.toList())
            Kind.Singleton -> createSingletonBeanModule(
                klass,
                instance,
                scopeQualifier,
                qualifier,
                bean.second.toList()
            )

            else -> {
                return
            }
        }
        getKoin().loadModules(listOf(module))
    }

    private fun <T : Any> createSingletonBeanModule(
        klass: KClass<T>,
        instance: Any,
        scopeQualifier: Qualifier = getKoin().scopeRegistry.rootScope.scopeQualifier,
        qualifier: Qualifier? = null,
        secondaryTypes: List<KClass<*>>,
        createdAtStart: Boolean = false
    ): Module {
        val beanDefinition =
            BeanDefinition(scopeQualifier, klass, qualifier, { instance }, Kind.Singleton, secondaryTypes)
        val singletonInstanceFactory = SingleInstanceFactory(beanDefinition)
        return Module(createdAtStart).apply {
            indexPrimaryType(singletonInstanceFactory)
            indexSecondaryTypes(singletonInstanceFactory)
        }
    }

    private fun <T : Any> createFactoryBeanModule(
        klass: KClass<T>,
        instance: Any,
        scopeQualifier: Qualifier = getKoin().scopeRegistry.rootScope.scopeQualifier,
        qualifier: Qualifier? = null,
        secondaryTypes: List<KClass<*>>,
        createdAtStart: Boolean = false
    ): Module {
        val beanDefinition =
            BeanDefinition(scopeQualifier, klass, qualifier, { instance }, Kind.Factory, secondaryTypes)
        val factoryInstanceFactory = FactoryInstanceFactory(beanDefinition)
        return Module(createdAtStart).apply {
            indexPrimaryType(factoryInstanceFactory)
            indexSecondaryTypes(factoryInstanceFactory)
        }
    }

    /**
     * 스코프 Qualifier 를 구합니다.
     */
    private fun getScopeQualifier(element: KAnnotatedElement): Qualifier {
        val scopeAnnotation = element.findAnnotation<Scope>()
        return if (scopeAnnotation?.value != null) {
            TypeQualifier(scopeAnnotation.value)
        } else if (scopeAnnotation?.name != null) {
            StringQualifier(scopeAnnotation.name)
        } else {
            getKoin().scopeRegistry.rootScope.scopeQualifier
        }
    }

    /**
     * Qualifier 를 구합니다.
     */
    private fun getQualifier(element: KAnnotatedElement): Qualifier? {
        return element.findAnnotation<Named>()?.value?.let { StringQualifier(it) }
    }

    /**
     * 빈의 종류와 Bind 타입들을 구합니다.
     */
    private fun getBeanProperties(klass: KClass<*>): Pair<Kind, Array<KClass<*>>>? {
        val factory = klass.findAnnotation<HQFactory>()
        val single = klass.findAnnotation<HQSingleton>()
        if (factory != null && single != null) {
            throw IllegalArgumentException("Factory 와 Single(ton) 은 공존할 수 없습니다.")
        }

        return when (true) {
            (factory != null) -> Pair(Kind.Factory, factory.binds)
            (single != null) -> Pair(Kind.Singleton, single.binds)
            else -> null
        }
    }
}