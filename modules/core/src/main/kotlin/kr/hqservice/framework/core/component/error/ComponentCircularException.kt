package kr.hqservice.framework.core.component.error

import kotlin.reflect.KClass

class ComponentCircularException(val classes: List<KClass<*>>) : Exception(
    "circular dependencies found: " +
            classes.map { "\n$it" }.toString().replace("[", "").replace("]", "") + "\n" +
            ""
)