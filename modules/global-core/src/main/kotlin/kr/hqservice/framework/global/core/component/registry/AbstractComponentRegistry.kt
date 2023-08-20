package kr.hqservice.framework.global.core.component.registry

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import kr.hqservice.framework.global.core.component.*
import kr.hqservice.framework.global.core.component.error.*
import kr.hqservice.framework.global.core.component.handler.AnnotationHandler
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQAnnotationHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import kr.hqservice.framework.global.core.extension.print
import kr.hqservice.framework.global.core.util.AnsiColor
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
        val annotationHandlers: MutableMap<KClass<HQAnnotationHandler<*>>, HQAnnotationHandler<*>> = mutableMapOf()
        val qualifierProviders: MutableMap<String, MutableNamedProvider> = mutableMapOf()
    }

    private val componentInstances: ComponentInstanceMap = ComponentInstanceMap()
    private val annotationProcessNeededInstancesMap: Multimap<KClass<out Annotation>, Any> = ArrayListMultimap.create()

    abstract fun getProvidedInstances(): MutableMap<KClass<*>, out Any>

    abstract fun getConfiguration(): HQYamlConfiguration

    @Suppress("UNCHECKED_CAST")
    final override fun setup() {
        val componentClasses = mutableListOf<Class<*>>()
        val beanClasses = mutableListOf<Class<*>>()
        val configurationClasses = mutableListOf<Class<*>>()
        val qualifierProviderClasses = mutableListOf<Class<*>>()
        val unsortedComponentHandlers: MutableMap<KClass<*>, KClass<HQComponentHandler<*>>> = mutableMapOf()
        val unsortedAnnotationHandlers: MutableMap<KClass<*>, KClass<HQAnnotationHandler<*>>> = mutableMapOf()
        for (clazz in getAllComponentsToScan()) {
            val annotations = clazz.annotations
            if (annotations.filterIsInstance<ComponentHandler>().isNotEmpty()) {
                val componentHandlerType = getComponentHandlerTypeParameter(clazz.kotlin)
                unsortedComponentHandlers[componentHandlerType] = clazz.kotlin as KClass<HQComponentHandler<*>>
            } else if (annotations.filterIsInstance<Component>().isNotEmpty()) {
                componentClasses.add(clazz)
            } else if (annotations.filterIsInstance<QualifierProvider>().isNotEmpty()) {
                qualifierProviderClasses.add(clazz)
            } else if (annotations.filterIsInstance<Bean>().isNotEmpty()) {
                beanClasses.add(clazz)
            } else if (annotations.filterIsInstance<Configuration>().isNotEmpty()) {
                configurationClasses.add(clazz)
            } else if (annotations.any { it.annotationClass.hasAnnotation<Scannable>() }) {
                componentClasses.add(clazz)
            } else if (annotations.filterIsInstance<AnnotationHandler>().isNotEmpty()) {
                val annotationHandlerType = getAnnotationHandlerTypeParameter(clazz.kotlin)
                unsortedAnnotationHandlers[annotationHandlerType] = clazz.kotlin as KClass<HQAnnotationHandler<*>>
            }
        }

        val componentClassesQueue: ConcurrentLinkedQueue<KClass<*>> = ConcurrentLinkedQueue(
            componentClasses.map { it.kotlin }
        ).apply {
            addAll(qualifierProviderClasses.map { it.kotlin })
            addAll(beanClasses.map { it.kotlin })
            addAll(configurationClasses.map { it.kotlin })
        }

        // 사이즈가 같은 채로 그 큐의 사이즈만큼 반복됐다면, 더 이상 definition 이 없는것으로 판단 후 throw
        var componentExceptionCatchingStack = 0
        var previousComponentQueueSize = componentClassesQueue.size
        queue@ while (componentClassesQueue.isNotEmpty()) {
            val component = componentClassesQueue.poll()

            fun back() {
                componentClassesQueue.offer(component)
                if (previousComponentQueueSize == componentClassesQueue.size) {
                    componentExceptionCatchingStack++
                }
                if (componentExceptionCatchingStack == componentClassesQueue.size) {
                    printFriendlyException(componentClassesQueue.toList())
                    throw NoBeanDefinitionsFoundException()
                }
                previousComponentQueueSize = componentClassesQueue.size
            }

            val instance = try {
                if (component.hasAnnotation<Bean>()) {
                    tryCreateBeanModule(component, component) {
                        callByInjectedParameters(component.constructors.first())
                    }
                    continue
                } else if (component.hasAnnotation<Configuration>()) {
                    val instance = callByInjectedParameters(component.constructors.first())
                    if (instance == null) {
                        back()
                        continue@queue
                    }
                    val methods = component.declaredFunctions.filter {
                        BeanProperty.findBeanProperty(it) != null || it.hasAnnotation<Bean>()
                    }
                    val definitions: MutableMap<KClass<*>, Pair<KAnnotatedElement, Any>> = mutableMapOf()
                    for (kFunction in methods) {
                        val injected = injectParameters(kFunction)
                        if (injected.any { it == null }) {
                            back()
                            continue@queue
                        }
                        val called = kFunction.call(instance, *injected.toTypedArray())
                            ?: throw IllegalStateException("null 은 bean 으로 선언될 수 없습니다.")
                        definitions[kFunction.returnType.jvmErasure] = kFunction to called
                    }
                    definitions.forEach { (klass, pair) ->
                        tryCreateBeanModule(pair.first, klass) { pair.second }
                    }
                }

                if (component.constructors.size > 1) {
                    throw ConstructorConflictException(component)
                }
                if (component.objectInstance == null) {
                    callByInjectedParameters(component.constructors.first())
                } else {
                    component.objectInstance!!
                }
            } catch (exception: QualifierNotFoundException) {
                back()
                continue
            }

            if (instance == null) {
                back()
                continue
            } else {
                try {
                    tryCreateBeanModule(component, component) { instance }
                } catch (exception: QualifierNotFoundException) {
                    back()
                    continue
                }

                if (instance is HQComponent) {
                    componentInstances.addSafely(instance)
                } else if (instance is MutableNamedProvider) {
                    val key = component.annotations.filterIsInstance<QualifierProvider>().first().key
                    qualifierProviders[key] = instance
                } else if (component.annotations.any { it.annotationClass.hasAnnotation<Scannable>() }) {
                    component
                        .annotations
                        .filter { it.annotationClass.hasAnnotation<Scannable>() }
                        .forEach { annotation ->
                            annotationProcessNeededInstancesMap.put(annotation.annotationClass, instance)
                        }
                }

                componentExceptionCatchingStack = 0
            }
        }

        val annotationHandlersQueue: ConcurrentLinkedQueue<KClass<HQAnnotationHandler<*>>> =
            ConcurrentLinkedQueue(unsortedAnnotationHandlers.values.toMutableList() + annotationHandlers.keys)

        var previousHandlerQueueSize = annotationHandlersQueue.size
        var handlerExceptionCatchingStack = 0

        fun <T : Any> checkDefinitionNeedThrow(handlersQueue: ConcurrentLinkedQueue<KClass<T>>) {
            if (previousHandlerQueueSize == handlersQueue.size) {
                handlerExceptionCatchingStack++
            }
            if (handlerExceptionCatchingStack == handlersQueue.size) {
                printFriendlyException(handlersQueue.map { it::class })
                throw NoBeanDefinitionsFoundException()
            }
            previousHandlerQueueSize = handlersQueue.size
        }

        fun resetThrowStack() {
            handlerExceptionCatchingStack = 0
        }

        while (annotationHandlersQueue.isNotEmpty()) {
            val annotationHandlerClass = annotationHandlersQueue.poll()
            val annotationHandler = callByInjectedParameters(annotationHandlerClass.constructors.first())
            if (annotationHandler == null) {
                annotationHandlersQueue.offer(annotationHandlerClass)
                checkDefinitionNeedThrow(annotationHandlersQueue)
            } else {
                processAnnotationProcessNeedInstances(annotationHandler) { any, annotation ->
                    setup(any, annotation)
                }
                annotationHandlers[annotationHandlerClass] = annotationHandler
                resetThrowStack()
            }
        }

        val componentHandlersQueue: ConcurrentLinkedQueue<KClass<HQComponentHandler<*>>> =
            ConcurrentLinkedQueue(unsortedComponentHandlers.values.toMutableList() + componentHandlers.keys)
        previousComponentQueueSize = componentHandlersQueue.size
        resetThrowStack()

        while (componentHandlersQueue.isNotEmpty()) {
            val componentHandlerClass = componentHandlersQueue.poll()
            val depends = componentHandlerClass.findAnnotation<ComponentHandler>()!!.depends
            val any = depends.filter { depended ->
                componentHandlers[depended] == null
            }
            if (depends.isEmpty() || any.isEmpty()) {
                val componentHandler = callByInjectedParameters(componentHandlerClass.constructors.first())

                if (componentHandler == null) {
                    componentHandlersQueue.offer(componentHandlerClass)
                    checkDefinitionNeedThrow(componentHandlersQueue)
                } else {
                    processComponents(componentHandler) { component ->
                        setup(component)
                    }
                    componentHandlers[componentHandlerClass] = componentHandler
                    resetThrowStack()
                }
            } else {
                val illegalDepends = depends.filter {
                    !it.allSuperclasses.contains(HQComponentHandler::class)
                }
                if (illegalDepends.isNotEmpty()) {
                    throw IllegalDependException(illegalDepends)
                }

                componentHandlersQueue.offer(componentHandlerClass)
                checkDefinitionNeedThrow(componentHandlersQueue)
            }
        }
    }

    final override fun teardown() {
        val reversedAnnotationHandlers = annotationHandlers.toList().reversed().toMap()
        reversedAnnotationHandlers.forEach { (_, annotationHandler) ->
            processAnnotationProcessNeedInstances(annotationHandler) { any, annotation ->
                teardown(any, annotation)
            }
        }
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
        val handlerType = getComponentHandlerTypeParameter(componentHandler::class)
        componentInstances.filter { it.key.allSuperclasses.contains(handlerType) }.forEach { (_, component) ->
            @Suppress("UNCHECKED_CAST")
            componentHandler as HQComponentHandler<HQComponent>
            componentHandler.action(component)
        }
    }

    private fun processAnnotationProcessNeedInstances(
        annotationHandler: HQAnnotationHandler<*>,
        action: HQAnnotationHandler<Annotation>.(Any, Annotation) -> Unit
    ) {
        val handlerType = getAnnotationHandlerTypeParameter(annotationHandler::class)
        annotationProcessNeededInstancesMap
            .run {
                this.entries().filter { it.key == handlerType }
            }.forEach {
                val annotation = it.key
                val any = it.value
                @Suppress("UNCHECKED_CAST")
                annotationHandler as HQAnnotationHandler<Annotation>
                annotationHandler.action(any, any::class.findAnnotations(annotation).single())
            }
    }


    override fun <T : HQComponent> getComponent(key: KClass<T>): T {
        return componentInstances.getComponent(key)
    }

    private fun printFriendlyException(classes: List<KClass<*>>) {
        classes.forEach { kClass ->
            val parameters = kClass.primaryConstructor?.valueParameters ?: listOf()
            val injected = injectParameters(kClass.primaryConstructor!!)
            val parameterDisplays = parameters.mapIndexed { index, kParameter ->
                val simpleName = kParameter.type.jvmErasure.simpleName
                val qualifier = getQualifier(kParameter)?.value
                val color = if (injected[index] != null) AnsiColor.GREEN else AnsiColor.RED
                if (qualifier != null) {
                    "${color}${simpleName}(q: ${qualifier})${AnsiColor.RESET}"
                } else {
                    "${color}${simpleName}${AnsiColor.RESET}"
                }
            }

            println("${kClass.java.packageName}.${AnsiColor.CYAN}${kClass.simpleName}${AnsiColor.RESET}: [${parameterDisplays.joinToString()}]")
        }
    }

    private fun getComponentHandlerTypeParameter(componentHandlerClass: KClass<*>): KClass<*> {
        return componentHandlerClass
            .supertypes
            .first { it.isSubtypeOf(HQComponentHandler::class.starProjectedType) }
            .arguments
            .first()
            .type!!.jvmErasure
    }

    private fun getAnnotationHandlerTypeParameter(annotationHandlerClass: KClass<*>): KClass<*> {
        return annotationHandlerClass
            .supertypes
            .first { it.isSubtypeOf(HQAnnotationHandler::class.starProjectedType) }
            .arguments
            .first()
            .type!!.jvmErasure
    }

    /**
     * @return 만약에 파라미터에 맞는 값을 찾을 수 없을 경우, null 을 리턴합니다.
     */
    private fun <T> callByInjectedParameters(
        kFunction: KFunction<T>,
        providedInstanceMap: Map<KClass<*>, *>? = getProvidedInstances()
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
        return kFunction.valueParameters.mapIndexed { index, parameter ->
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
                null
            } catch (exception: IllegalStateException) {
                null
            }
        }.toList()
    }

    abstract fun getAllComponentsToScan(): Collection<Class<*>>

    /**
     * 모듈을 생성합니다. 만약 아무런 타입의 Bean 도 아닐 경우, 생성되지 않습니다.
     */
    private fun <T> tryCreateBeanModule(
        annotatedElement: KAnnotatedElement,
        primaryBind: KClass<*>,
        instance: Definition<T>
    ) {
        val scopeQualifier = getScopeQualifier(annotatedElement)
        val qualifier = getQualifier(annotatedElement)
        val property = BeanProperty.getBeanProperty(annotatedElement)
        val secondaryTypes: List<KClass<*>> = property.binds.ifEmpty {
            primaryBind.allSuperclasses.toList()
        }

        val module = when (property.kind) {
            Kind.Factory -> createFactoryBeanModule(
                primaryBind,
                instance,
                scopeQualifier,
                qualifier,
                secondaryTypes
            )

            Kind.Singleton -> createSingletonBeanModule(
                primaryBind,
                instance,
                scopeQualifier,
                qualifier,
                secondaryTypes
            )

            Kind.Scoped -> throw UnsupportedOperationException()
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

    private class BeanProperty(
        val kind: Kind,
        val binds: List<KClass<*>>,
        val isPrimary: Boolean
    ) {
        companion object {
            fun isPrimary(kAnnotatedElement: KAnnotatedElement): Boolean {
                return kAnnotatedElement.findAnnotation<Primary>() != null
            }

            /**
             * 빈의 종류와 Bind 타입들을 구합니다. Kind 를 못찾을 경우, Singleton 기본 프로퍼티를 반환합니다.
             */
            fun getBeanProperty(kAnnotatedElement: KAnnotatedElement): BeanProperty {
                return findBeanProperty(kAnnotatedElement) ?: BeanProperty(
                    Kind.Singleton,
                    emptyList(),
                    isPrimary(kAnnotatedElement)
                )
            }

            /**
             * 빈의 종류와 Bind 타입들을 구합니다. Kind 를 못찾을 경우, null 을 반환합니다.
             */
            fun findBeanProperty(kAnnotatedElement: KAnnotatedElement): BeanProperty? {
                val factory = kAnnotatedElement.findAnnotation<Factory>()
                val single = kAnnotatedElement.findAnnotation<Singleton>()
                if (factory != null && single != null) {
                    throw IllegalArgumentException("Factory 와 Single(ton) 은 공존할 수 없습니다.")
                }
                val isPrimary = isPrimary(kAnnotatedElement)
                return when (true) {
                    (factory != null) -> BeanProperty(Kind.Factory, factory.binds.toList(), isPrimary)
                    (single != null) -> BeanProperty(Kind.Singleton, single.binds.toList(), isPrimary)
                    else -> null
                }
            }
        }
    }
}