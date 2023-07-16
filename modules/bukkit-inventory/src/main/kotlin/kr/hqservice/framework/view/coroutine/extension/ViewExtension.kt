package kr.hqservice.framework.view.coroutine.extension

import kr.hqservice.framework.view.HQView
import kr.hqservice.framework.view.HQViewModel
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.java.KoinJavaComponent
import org.koin.mp.KoinPlatformTools

@OptIn(KoinInternalApi::class)
inline fun <reified T : HQViewModel> HQView.viewModels(
    qualifier: Qualifier? = null,
    mode: LazyThreadSafetyMode = KoinPlatformTools.defaultLazyMode(),
    noinline parameters: ParametersDefinition? = null
): Lazy<T> {
    return KoinJavaComponent.getKoin().scopeRegistry.rootScope.inject<T>(qualifier, mode, parameters).apply {
        this@viewModels.ownedLifecycles.add(this.value)
    }
}