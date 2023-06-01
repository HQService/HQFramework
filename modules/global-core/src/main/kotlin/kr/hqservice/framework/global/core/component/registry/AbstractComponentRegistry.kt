package kr.hqservice.framework.global.core.component.registry

import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import kr.hqservice.framework.global.core.component.*
import kr.hqservice.framework.global.core.component.Factory
import kr.hqservice.framework.global.core.component.Singleton
import kr.hqservice.framework.global.core.component.error.*
import kr.hqservice.framework.global.core.extension.print
import org.koin.core.annotation.*
import org.koin.core.component.KoinComponent
import org.koin.core.definition.BeanDefinition
import org.koin.core.definition.Kind
import org.koin.core.definition.indexKey
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
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

@OptIn(KoinInternalApi::class)
abstract class AbstractComponentRegistry : ComponentRegistry, KoinComponent {
    private val componentInstances: ComponentInstanceMap = ComponentInstanceMap()
    private val componentHandlers: MutableMap<KClass<out HQComponentHandler<*>>, HQComponentHandler<*>> = mutableMapOf()
    private val qualifierProviders: MutableMap<String, MutableNamedProvider> = mutableMapOf()

    abstract fun getProvidedInstances(): MutableMap<KClass<*>, out Any>

    @Suppress("UNCHECKED_CAST")
    final override fun setup() {
        val componentClasses = mutableListOf<Class<*>>()
        val unsortedComponentHandlers: MutableMap<KClass<*>, KClass<HQComponentHandler<*>>> = mutableMapOf()
        for (clazz in getAllComponentsToScan()) {
            val annotations = clazz.annotations

            if (annotations.filterIsInstance<ComponentHandler>().isNotEmpty()) {
                val componentHandlerType = getComponentHandlerType(clazz.kotlin)
                unsortedComponentHandlers[componentHandlerType] = clazz.kotlin as KClass<HQComponentHandler<*>>
            } else if (annotations.filterIsInstance<Component>().isNotEmpty()) {
                componentClasses.add(clazz)
            } else if (annotations.filterIsInstance<QualifierProvider>().isNotEmpty()) {
                val key = annotations.filterIsInstance<QualifierProvider>().first().key
                val qualifierProvider = callByInjectedParameters(
                    clazz.kotlin.constructors.first(),
                    getProvidedInstances()
                ) as? MutableNamedProvider ?: throw IllegalStateException("not qualifier provider")
                qualifierProviders[key] = qualifierProvider
            }
        }
        val componentClassesQueue: ConcurrentLinkedQueue<KClass<*>> =
            ConcurrentLinkedQueue(componentClasses.map { it.kotlin })

        // 사이즈가 같은 채로 그 큐의 사이즈만큼 반복됐다면, 더 이상 definition 이 없는것으로 판단 후 throw
        var componentExceptionCatchingStack = 0
        var previousComponentQueueSize = componentClassesQueue.size
        while (componentClassesQueue.isNotEmpty()) {
            val component = componentClassesQueue.poll()
            if (component.constructors.size > 1) {
                throw ConstructorConflictException(component)
            }

            val constructor = component.constructors.first()
            val instance = callByInjectedParameters(constructor, getProvidedInstances())
            if (instance == null) {
                componentClassesQueue.offer(component)
                if (previousComponentQueueSize == componentClassesQueue.size) {
                    componentExceptionCatchingStack++
                }
                if (componentExceptionCatchingStack == componentClassesQueue.size) {
                    throw NoBeanDefinitionsFoundException(componentClassesQueue.toList())
                }
                previousComponentQueueSize = componentClassesQueue.size
            } else {
                componentInstances.addSafely(instance)
                tryCreateBeanModule(component, instance)
            }
        }
        val componentHandlersQueue: ConcurrentLinkedQueue<KClass<HQComponentHandler<*>>> = ConcurrentLinkedQueue(unsortedComponentHandlers.values)

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
                ) ?: throw NotComponentHandlerException(componentHandlerClass)
                processComponents(componentHandler) { component ->
                    setup(component)
                }
                componentHandlers[componentHandlerClass] = componentHandler
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
        val reversedComponentHandlers = this.componentHandlers.toList().reversed().toMap()
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

    @Suppress("UNCHECKED_CAST")
    private fun getComponentHandlerType(componentHandlerClass: KClass<*>): KClass<*> {
        return componentHandlerClass
            .supertypes
            .first()
            .arguments
            .first()
            .type?.jvmErasure ?: throw NullPointerException("ComponentHandlers must have one type parameter.")
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
                val providedInstance =
                    providedInstanceMap.filter { parameterKClass.isSubclassOf(it.key) }.values.firstOrNull()
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

    abstract fun getAllComponentsToScan(): Collection<Class<*>>

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
        return if (element.hasAnnotation<Named>()) {
            StringQualifier(element.findAnnotation<Named>()!!.value)
        } else if (element.hasAnnotation<MutableNamed>()) {
            val key = element.findAnnotation<MutableNamed>()!!.key
            val qualifierProvider =
                qualifierProviders[key] ?: throw NullPointerException("MutableNamedQualifier not found")
            val provided = qualifierProvider.provideQualifier()
            StringQualifier(provided)
        } else {
            null
        }
    }

    /**
     * 빈의 종류와 Bind 타입들을 구합니다.
     */
    private fun getBeanProperties(klass: KClass<*>): Pair<Kind, Array<KClass<*>>>? {
        val factory = klass.findAnnotation<Factory>()
        val single = klass.findAnnotation<Singleton>()
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