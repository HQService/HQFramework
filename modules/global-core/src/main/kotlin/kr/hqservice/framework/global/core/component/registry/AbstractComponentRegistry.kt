package kr.hqservice.framework.global.core.component.registry

import kr.hqservice.framework.global.core.component.*
import kr.hqservice.framework.global.core.component.error.*
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import kr.hqservice.framework.global.core.extension.print
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.annotation.Named
import org.koin.core.annotation.Scope
import org.koin.core.component.KoinComponent
import org.koin.core.definition.BeanDefinition
import org.koin.core.definition.Definition
import org.koin.core.definition.Kind
import org.koin.core.definition.indexKey
import org.koin.core.error.DefinitionOverrideException
import org.koin.core.error.InstanceCreationException
import org.koin.core.instance.FactoryInstanceFactory
import org.koin.core.instance.InstanceContext
import org.koin.core.instance.SingleInstanceFactory
import org.koin.core.module.Module
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.TypeQualifier
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

@OptIn(KoinInternalApi::class)
abstract class AbstractComponentRegistry : ComponentRegistry, KoinComponent {
    private companion object {
        val componentHandlers: MutableMap<KClass<HQComponentHandler<*>>, HQComponentHandler<*>> = mutableMapOf()
        val qualifierProviders: MutableMap<String, MutableNamedProvider> = mutableMapOf()
    }

    private val componentInstances: ComponentInstanceMap = ComponentInstanceMap()

    abstract fun getProvidedInstances(): MutableMap<KClass<*>, out Any>

    abstract fun getConfiguration(): HQYamlConfiguration

    @Suppress("UNCHECKED_CAST")
    final override fun setup() {
        val componentClasses = mutableListOf<Class<*>>()
        val beanClasses = mutableListOf<Class<*>>()
        val qualifierProviderClasses = mutableListOf<Class<*>>()
        val unsortedComponentHandlers: MutableMap<KClass<*>, KClass<HQComponentHandler<*>>> = mutableMapOf()
        for (clazz in getAllComponentsToScan()) {
            val annotations = clazz.annotations
            if (annotations.filterIsInstance<ComponentHandler>().isNotEmpty()) {
                val componentHandlerType = getComponentHandlerType(clazz.kotlin)
                unsortedComponentHandlers[componentHandlerType] = clazz.kotlin as KClass<HQComponentHandler<*>>
            } else if (annotations.filterIsInstance<Component>().isNotEmpty()) {
                componentClasses.add(clazz)
            } else if (annotations.filterIsInstance<QualifierProvider>().isNotEmpty()) {
                qualifierProviderClasses.add(clazz)
            } else if (annotations.filterIsInstance<Bean>().isNotEmpty()) {
                beanClasses.add(clazz)
            }
        }
        val componentClassesQueue: ConcurrentLinkedQueue<KClass<*>> = ConcurrentLinkedQueue(
            componentClasses.map { it.kotlin }
        ).apply {
            addAll(qualifierProviderClasses.map { it.kotlin })
            addAll(beanClasses.map { it.kotlin })
        }

        // 사이즈가 같은 채로 그 큐의 사이즈만큼 반복됐다면, 더 이상 definition 이 없는것으로 판단 후 throw
        var componentExceptionCatchingStack = 0
        var previousComponentQueueSize = componentClassesQueue.size
        while (componentClassesQueue.isNotEmpty()) {
            val component = componentClassesQueue.poll()

            fun back() {
                componentClassesQueue.offer(component)
                if (previousComponentQueueSize == componentClassesQueue.size) {
                    componentExceptionCatchingStack++
                }
                if (componentExceptionCatchingStack == componentClassesQueue.size) {
                    // printFriendlyException(componentClassesQueue.toList())
                    throw NoBeanDefinitionsFoundException(listOf())
                }
                previousComponentQueueSize = componentClassesQueue.size
            }

            val instance = try {
                if (component.hasAnnotation<Bean>()) {
                    tryCreateBeanModule(component) {
                        callByInjectedParameters(component.constructors.first(), getProvidedInstances())
                    }
                    continue
                }
                if (component.constructors.size > 1) {
                    throw ConstructorConflictException(component)
                }
                callByInjectedParameters(component.constructors.first(), getProvidedInstances())
            } catch (exception: QualifierNotFoundException) {
                back()
                continue
            } catch (exception: ComponentCreationException) {
                back()
                continue
            }

            if (instance == null) {
                back()
                continue
            } else {
                try {
                    tryCreateBeanModule(component) { instance }
                } catch (exception: QualifierNotFoundException) {
                    back()
                    continue
                }
                if (instance is HQComponent) {
                    componentInstances.addSafely(instance)
                } else if (instance is MutableNamedProvider) {
                    val key = component.annotations.filterIsInstance<QualifierProvider>().first().key
                    qualifierProviders[key] = instance
                }

                componentExceptionCatchingStack = 0
            }
        }
        val componentHandlersQueue: ConcurrentLinkedQueue<KClass<HQComponentHandler<*>>> =
            ConcurrentLinkedQueue(unsortedComponentHandlers.values.toMutableList() + componentHandlers.keys)
        var handlerExceptionCatchingStack = 0
        var previousHandlerQueueSize = componentHandlersQueue.size

        while (componentHandlersQueue.isNotEmpty()) {
            val componentHandlerClass = componentHandlersQueue.poll()
            val depends = componentHandlerClass.findAnnotation<ComponentHandler>()!!.depends
            val any = depends.filter { depended ->
                componentHandlers[depended] == null
            }
            if (depends.isEmpty() || any.isEmpty()) {
                val componentHandler = callByInjectedParameters(
                    componentHandlerClass.constructors.first(),
                    getProvidedInstances()
                )

                if (componentHandler == null) {
                    componentHandlersQueue.offer(componentHandlerClass)
                    if (previousHandlerQueueSize == componentHandlersQueue.size) {
                        handlerExceptionCatchingStack++
                    }
                    if (handlerExceptionCatchingStack == componentHandlersQueue.size) {
                        printFriendlyException(componentHandlersQueue.toList().map { it::class })
                        throw NoBeanDefinitionsFoundException(listOf())
                    }
                    previousHandlerQueueSize = componentHandlersQueue.size
                } else {
                    processComponents(componentHandler) { component ->
                        setup(component)
                    }
                    componentHandlers[componentHandlerClass] = componentHandler
                    handlerExceptionCatchingStack = 0
                }
            } else {
                val illegalDepends = depends.filter {
                    !it.allSuperclasses.contains(HQComponentHandler::class)
                }
                if (illegalDepends.isNotEmpty()) {
                    throw IllegalDependException(illegalDepends)
                }

                componentHandlersQueue.offer(componentHandlerClass)
                if (previousHandlerQueueSize == componentHandlersQueue.size) {
                    handlerExceptionCatchingStack++
                }
                if (handlerExceptionCatchingStack == componentHandlersQueue.size) {
                    throw ComponentCircularException(componentHandlersQueue.toList().map { it::class })
                }
                previousHandlerQueueSize = componentHandlersQueue.size
            }
        }
    }

    final override fun teardown() {
        val reversedComponentHandlers = componentHandlers.toList().reversed().toMap()
        reversedComponentHandlers.forEach { (_, componentHandler) ->
            processComponents(componentHandler) { component ->
                teardown(component)
            }
        }
    }

    private fun processComponents(
        componentHandler: HQComponentHandler<*>,
        action: HQComponentHandler<HQComponent>.(HQComponent) -> Unit
    ) {
        val handlerClass = componentHandler::class
        val handlerType = getComponentHandlerType(handlerClass)
        componentInstances.forEach { (componentClass, component) ->
            if (componentClass.allSuperclasses.contains(handlerType)) {
                @Suppress("UNCHECKED_CAST")
                componentHandler as HQComponentHandler<HQComponent>
                componentHandler.action(component)
            }
        }
    }

    override fun <T : HQComponent> getComponent(key: KClass<T>): T {
        return componentInstances.getComponent(key)
    }

    private object Color {
        const val ANSI_RESET = "\u001B[0m"
        const val ANSI_RED = "\u001B[31m"
        const val ANSI_GREEN = "\u001B[32m"
        const val ANSI_CYAN = "\u001B[36m"
    }

    private fun printFriendlyException(classes: List<KClass<*>>) {
        classes.forEach { kClass ->
            val parameters = kClass.primaryConstructor?.valueParameters ?: listOf()
            val injected = injectParameters(kClass.primaryConstructor!!, getProvidedInstances())
            val parameterDisplays = parameters.mapIndexed { index, kParameter ->
                val simpleName = kParameter.type.jvmErasure.simpleName
                val qualifier = getQualifier(kParameter)?.value
                val color = if (injected[index] != null) Color.ANSI_GREEN else Color.ANSI_RED
                if (qualifier != null) {
                    "${color}${simpleName}(q: ${qualifier})${Color.ANSI_RESET}"
                } else {
                    "${color}${simpleName}${Color.ANSI_RESET}"
                }
            }

            println("${kClass.java.packageName}.${Color.ANSI_CYAN}${kClass.simpleName}${Color.ANSI_RESET}: [${parameterDisplays.joinToString()}]")
        }
    }

    private fun getComponentHandlerType(componentHandlerClass: KClass<*>): KClass<*> {
        return componentHandlerClass
            .supertypes
            .first { it.isSubtypeOf(HQComponentHandler::class.starProjectedType) }
            .arguments
            .first()
            .type?.jvmErasure ?: throw NullPointerException("ComponentHandlers must have one type parameter.")
    }

    /**
     * @return 만약에 파라미터에 맞는 값을 찾을 수 없을 경우, null 을 리턴합니다.
     */
    private fun <T : Any> callByInjectedParameters(
        kFunction: KFunction<T>,
        providedInstanceMap: Map<KClass<*>, *>? = null,
    ): T? {
        val injectedParameters = injectParameters(kFunction, providedInstanceMap)
        if (injectedParameters.any { it == null }) {
            return null
        }
        return try {
            kFunction.call(*injectedParameters.toTypedArray())
        } catch (illegalArgumentException: IllegalArgumentException) {
            kFunction
                .instanceParameter
                .print("instanceParameter: ")
            kFunction
                .parameters
                .map { it.type.jvmErasure.simpleName }
                .joinToString()
                .print("parameters: ")
            injectedParameters
                .map { if (it == null) null else it::class.simpleName }
                .joinToString()
                .print("injectedParameters: ")
            throw illegalArgumentException
        }
    }

    open fun injectProxy(
        kParameter: KParameter,
        qualifier: Qualifier?,
        scopeQualifier: Qualifier?
    ): Any? {
        return null
    }

    private fun injectParameters(
        kFunction: KFunction<*>,
        providedInstanceMap: Map<KClass<*>, *>? = null,
    ): List<Any?> {
        return kFunction.parameters.mapIndexed { index, parameter ->
            val parameterKClass = parameter.type.classifier as KClass<*>

            if (providedInstanceMap != null) {
                val providedInstance = providedInstanceMap.filter { parameterKClass == it.key }.values.firstOrNull()
                if (providedInstance != null) {
                    return@mapIndexed providedInstance
                }
            }

            val qualifier = getQualifier(parameter)
            val scopeQualifier = getScopeQualifier(kFunction)
            val indexKey = indexKey(parameterKClass, qualifier, scopeQualifier)

            val proxy = injectProxy(parameter, qualifier, scopeQualifier)
            if (proxy != null) {
                return@mapIndexed proxy
            }
            val factory = getKoin().instanceRegistry.instances[indexKey]
            val defaultContext = InstanceContext(getKoin(), getKoin().getScope(scopeQualifier.value))

            try {
                factory?.get(defaultContext)
            } catch (exception: InstanceCreationException) {
                throw ComponentCreationException()
            } catch (exception: IllegalStateException) {
                throw ComponentCreationException()
            }
        }.toList()
    }

    abstract fun getAllComponentsToScan(): Collection<Class<*>>

    /**
     * 모듈을 생성합니다. 만약 아무런 타입의 Bean 도 아닐 경우, 생성되지 않습니다.
     */
    private fun <T> tryCreateBeanModule(klass: KClass<*>, instance: Definition<T>) {
        val scopeQualifier = getScopeQualifier(klass)
        val qualifier = getQualifier(klass)
        val property = getBeanProperties(klass)
        val secondaryTypes: List<KClass<*>> = property.binds.ifEmpty {
            klass.allSuperclasses.toList()
        }

        val module = when (property.kind) {
            Kind.Factory -> createFactoryBeanModule(
                klass,
                instance,
                scopeQualifier,
                qualifier,
                secondaryTypes
            )

            Kind.Singleton -> createSingletonBeanModule(
                klass,
                instance,
                scopeQualifier,
                qualifier,
                secondaryTypes
            )

            Kind.Scoped -> return
        }
        getKoin().loadModules(listOf(module), allowOverride = !property.isPrimary)
    }

    private fun <T> createSingletonBeanModule(
        klass: KClass<*>,
        instance: Definition<T>,
        scopeQualifier: Qualifier = getKoin().scopeRegistry.rootScope.scopeQualifier,
        qualifier: Qualifier? = null,
        secondaryTypes: List<KClass<*>>,
        createdAtStart: Boolean = false
    ): Module {
        val beanDefinition = BeanDefinition(scopeQualifier, klass, qualifier, instance, Kind.Singleton, secondaryTypes)
        val singletonInstanceFactory = SingleInstanceFactory(beanDefinition)
        return Module(createdAtStart).apply {
            try {
                indexPrimaryType(singletonInstanceFactory)
                indexSecondaryTypes(singletonInstanceFactory)
            } catch (_: DefinitionOverrideException) {
            }

        }
    }

    private fun <T> createFactoryBeanModule(
        klass: KClass<*>,
        instance: Definition<T>,
        scopeQualifier: Qualifier = getKoin().scopeRegistry.rootScope.scopeQualifier,
        qualifier: Qualifier? = null,
        secondaryTypes: List<KClass<*>>,
        createdAtStart: Boolean = false
    ): Module {
        val beanDefinition = BeanDefinition(scopeQualifier, klass, qualifier, instance, Kind.Factory, secondaryTypes)
        val factoryInstanceFactory = FactoryInstanceFactory(beanDefinition)
        return Module(createdAtStart).apply {
            try {
                indexPrimaryType(factoryInstanceFactory)
                indexSecondaryTypes(factoryInstanceFactory)
            } catch (_: DefinitionOverrideException) {
            }
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
        return if (element.hasAnnotation<Named>()) {
            StringQualifier(element.findAnnotation<Named>()!!.value)
        } else if (element.hasAnnotation<MutableNamed>()) {
            val key = element.findAnnotation<MutableNamed>()!!.key
            val qualifierProvider = qualifierProviders[key] ?: throw QualifierNotFoundException()
            val provided = qualifierProvider.provideQualifier()
            StringQualifier(provided)
        } else if (element.hasAnnotation<kr.hqservice.framework.global.core.component.Qualifier>()) {
            val value = element.findAnnotation<kr.hqservice.framework.global.core.component.Qualifier>()!!.value
            if (value.startsWith("#")) {
                StringQualifier(getConfiguration().getString(value.removePrefix("#")))
            } else {
                StringQualifier(value)
            }
        } else {
            null
        }
    }

    /**
     * 빈의 종류와 Bind 타입들을 구합니다.
     */
    private fun getBeanProperties(klass: KClass<*>): BeanProperty {
        val factory = klass.findAnnotation<Factory>()
        val single = klass.findAnnotation<Singleton>()
        if (factory != null && single != null) {
            throw IllegalArgumentException("Factory 와 Single(ton) 은 공존할 수 없습니다.")
        }
        val isPrimary = klass.findAnnotation<Primary>() != null
        return when (true) {
            (factory != null) -> BeanProperty(Kind.Factory, factory.binds.toList(), isPrimary)
            (single != null) -> BeanProperty(Kind.Singleton, single.binds.toList(), isPrimary)
            else -> BeanProperty(
                Kind.Singleton,
                klass.allSuperclasses.toList(),
                isPrimary
            )
        }
    }

    private class BeanProperty(
        val kind: Kind,
        val binds: List<KClass<*>>,
        val isPrimary: Boolean
    )
}