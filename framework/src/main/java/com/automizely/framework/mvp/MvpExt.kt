package com.automizely.framework.mvp

import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

/**
 * Get Koin context
 */
fun getKoin() = GlobalContext.get()

/**
 * 使用 koin 注入 presenter 同时和 V 层绑定
 * 参考 ComponentCallbackExt.kt 中 ComponentCallbacks.get() 函数的实现
 */
inline fun <reified T : BaseMvpPresenter<*>> BaseMvpView.getPresenter(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): T {
    return getKoin().get<T>(qualifier, parameters).also { it.attach(this) }
}

/**
 * 使用 koin 注入 presenter 同时和 V 层绑定
 * 参考 ComponentCallbackExt.kt 中 ComponentCallbacks.inject() 函数的实现
 */
inline fun <reified T : BaseMvpPresenter<*>> BaseMvpView.injectPresenter(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) {
    getKoin().get<T>(qualifier, parameters).also { it.attach(this) }
}

object MvpExt {

    @JvmStatic
    @JvmOverloads
    fun <P : BaseMvpPresenter<*>, V : BaseMvpView> getPresenter(
        pClass: Class<P>,
        view: V,
        qualifier: Qualifier? = null,
        parameters: ParametersDefinition? = null
    ): P {
        return getKoin().get<P>(pClass.kotlin, qualifier, parameters).also { it.attach(view) }
    }

}