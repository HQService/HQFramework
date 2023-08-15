package kr.hqservice.framework.global.core.component.handler

interface HQAnnotationHandler<A : Annotation> {
    fun setup(obj: Any, annotation: A) {}

    fun teardown(obj: Any, annotation: A) {}
}