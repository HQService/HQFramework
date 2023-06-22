package kr.hqservice.framework.global.core.component.error

import kotlin.reflect.KClass

class NoBeanDefinitionsFoundException(classes: List<KClass<*>>) : DefinitionException("no definition found for classes", classes)