package kr.hqservice.framework.view.extension

import kr.hqservice.framework.view.View
import kr.hqservice.framework.view.ViewModel
import kr.hqservice.framework.view.ViewModelFactory
import org.koin.java.KoinJavaComponent.getKoin
import kotlin.reflect.KClass

val _viewModelFactory: ViewModelFactory by getKoin().inject()

fun <T : ViewModel> View.viewModels(modelClass: KClass<T>): Lazy<T> {
    return lazy {
        _viewModelFactory.provideViewModel(modelClass) as T
    }.apply {
        this@viewModels._childLifecycles.add(this.value)
    }
}