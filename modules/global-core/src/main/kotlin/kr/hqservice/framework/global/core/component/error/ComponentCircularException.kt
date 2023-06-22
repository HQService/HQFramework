package kr.hqservice.framework.global.core.component.error

import kotlin.reflect.KClass

class ComponentCircularException(classes: List<KClass<*>>) : DefinitionException("circular dependencies found", classes)