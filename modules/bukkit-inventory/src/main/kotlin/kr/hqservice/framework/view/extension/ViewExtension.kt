package kr.hqservice.framework.view.extension

import kr.hqservice.framework.view.View
import kr.hqservice.framework.view.ViewModel
import kr.hqservice.framework.view.ViewModelFactory
import org.koin.java.KoinJavaComponent.getKoin

//val _viewModelFactory: ViewModelFactory by getKoin().inject()

/*inline fun <reified T : ViewModel> View.viewModels(): Lazy<T> {
    return lazy {
        _viewModelFactory.provideViewModel(T::class) as T
    }.apply {
        this@viewModels._childLifecycles.add(this.value)
    }
}*/
