package com.automizely.framework.mvp

import org.koin.core.context.KoinContextHandler
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

/**
 * 使用 koin 注入 presenter 同时和 V 层绑定
 * 参考 ComponentCallbackExt.kt 中 ComponentCallbacks.inject() 函数的实现
 */
inline fun <reified T : BaseMvpPresenter<*>> BaseMvpView.injectPresenter(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) {
    KoinContextHandler.get().get<T>(qualifier, parameters).also { it.attach(this) }
}