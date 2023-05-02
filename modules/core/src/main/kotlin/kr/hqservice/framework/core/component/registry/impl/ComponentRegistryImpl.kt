package kr.hqservice.framework.core.component.registry.impl

import kr.hqservice.framework.core.component.Component
import kr.hqservice.framework.core.HQPlugin
import kr.hqservice.framework.core.component.registry.ComponentRegistry
import org.koin.core.annotation.*
import org.koin.core.component.KoinComponent
import org.koin.core.definition.BeanDefinition
import org.koin.core.definition.Kind
import org.koin.core.definition.indexKey
import org.koin.core.instance.FactoryInstanceFactory
import org.koin.core.module.Module
import org.koin.core.instance.InstanceFactory
import org.koin.core.instance.SingleInstanceFactory
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.TypeQualifier
import java.util.jar.JarFile
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

@OptIn(KoinInternalApi::class)
@Single(binds = [ComponentRegistry::class])
class ComponentRegistryImpl(private val plugin: HQPlugin) : ComponentRegistry, KoinComponent {
    private val components: MutableList<*> = mutableListOf<Any>()

    override fun processAnnotations() {
        val componentClasses = getAllPluginClasses()
        componentClasses.forEach { clazz ->
            for (annotation in clazz.annotations) {
                if (annotation is Component) {
//                     TODO: 컴포넌트 핸들러들의 타입들을 전부 가져와서, 컴포넌트 타입과 대조 후 그에 맞는 로직 실행
//                    if (validate(klass)) {
//                        registerComponent(klass)
//                    }
                }
            }
        }
    }

    private fun getAllPluginClasses(): Collection<KClass<*>> {
        val classes: MutableSet<KClass<*>> = mutableSetOf()
        val entries = JarFile(plugin.getJar()).entries()
        while (entries.hasMoreElements()) {
            val name = entries.nextElement().name.replace("/", ".")
            if (name.startsWith(plugin::class.java.packageName) && name.endsWith(".class")) {
                try {
                    val clazz = Class.forName(name.removeSuffix(".class"))
                    classes.add(clazz.kotlin)
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
    fun <T : Any> tryCreateBeanModule(klass: KClass<T>, instance: T) {
        val scopeQualifier = getScopeQualifier(klass)
        val qualifier = getQualifier(klass)
        val bean = getBeanProperties(klass) ?: return

        val module = when (bean.first) {
            Kind.Factory -> createFactoryBeanModule(klass, instance, scopeQualifier, qualifier, bean.second.toList())
            Kind.Singleton -> createSingletonBeanModule(klass, instance, scopeQualifier, qualifier, bean.second.toList())
            else -> { return }
        }
        getKoin().loadModules(listOf(module))
    }

    fun <T : Any> createSingletonBeanModule(
        klass: KClass<T>,
        instance: T,
        scopeQualifier: Qualifier = getKoin().scopeRegistry.rootScope.scopeQualifier,
        qualifier: Qualifier? = null,
        secondaryTypes: List<KClass<*>>,
        createdAtStart: Boolean = false
    ): Module {
        val beanDefinition = BeanDefinition(scopeQualifier, klass, qualifier, { instance }, Kind.Singleton, secondaryTypes)
        val singletonInstanceFactory = SingleInstanceFactory(beanDefinition)
        return Module(createdAtStart).apply {
            indexPrimaryType(singletonInstanceFactory)
            indexSecondaryTypes(singletonInstanceFactory)
        }
    }

    fun <T : Any> createFactoryBeanModule(
        klass: KClass<T>,
        instance: T,
        scopeQualifier: Qualifier = getKoin().scopeRegistry.rootScope.scopeQualifier,
        qualifier: Qualifier? = null,
        secondaryTypes: List<KClass<*>>,
        createdAtStart: Boolean = false
    ): Module {
        val beanDefinition = BeanDefinition(scopeQualifier, klass, qualifier, { instance }, Kind.Factory, secondaryTypes)
        val factoryInstanceFactory = FactoryInstanceFactory(beanDefinition)
        return Module(createdAtStart).apply {
            indexPrimaryType(factoryInstanceFactory)
            indexSecondaryTypes(factoryInstanceFactory)
        }
    }

    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    private fun <T> Module.indexSecondaryTypes(factory: InstanceFactory<T>) {
        factory.beanDefinition.secondaryTypes.forEach { secondClazz ->
            val mapping = indexKey(secondClazz, factory.beanDefinition.qualifier, factory.beanDefinition.scopeQualifier)
            saveMapping(mapping, factory)
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
        val factory = klass.findAnnotation<Factory>()
        val single = klass.findAnnotation<Single>()
        val singleton = klass.findAnnotation<Singleton>()
        if (factory != null && (single != null || singleton != null)) {
            throw IllegalArgumentException("Factory 와 Single(ton) 은 공존할 수 없습니다.")
        }

        return when(true) {
            (factory != null) -> Pair(Kind.Factory, factory.binds)
            (single != null) -> Pair(Kind.Singleton, single.binds)
            (singleton != null) -> Pair(Kind.Singleton, singleton.binds)
            else -> null
        }
    }
}