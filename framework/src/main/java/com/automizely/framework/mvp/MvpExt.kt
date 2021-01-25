package com.automizely.framework.mvp

import android.content.ComponentCallbacks
import androidx.lifecycle.Lifecycle
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
inline fun <reified V : BaseMvpView, reified T : BaseMvpPresenter<V>> ComponentCallbacks.getPresenter(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): T {
    val view = this as V
    return getKoin().get<T>(qualifier, parameters).also { it.attach(view) }
}

/**
 * 使用 koin 注入 presenter 同时和 V 层绑定
 * 参考 ComponentCallbackExt.kt 中 ComponentCallbacks.inject() 函数的实现
 */
inline fun <reified V : BaseMvpView, reified T : BaseMvpPresenter<V>> ComponentCallbacks.injectPresenter(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) {
    val view = this as V
    getKoin().get<T>(qualifier, parameters).also { it.attach(view) }
}

/**
 * 用于判断 view 的生命周期是否处于活跃状态
 */
fun BaseMvpView.isActive(): Boolean = getLifecycle().currentState.isAtLeast(Lifecycle.State.CREATED)

/**
 * 获取 view 的当前生命周期状态
 */
fun BaseMvpView.getCurrentState(): Lifecycle.State = getLifecycle().currentState

object MvpExt {

    @JvmStatic
    @JvmOverloads
    fun <V : BaseMvpView, P : BaseMvpPresenter<V>> getPresenter(
        pClass: Class<P>,
        view: V,
        qualifier: Qualifier? = null,
        parameters: ParametersDefinition? = null
    ): P {
        return getKoin().get<P>(pClass.kotlin, qualifier, parameters).also { it.attach(view) }
    }

}