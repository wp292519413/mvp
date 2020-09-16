package com.automizely.framework.base

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.koin.core.KoinComponent
import java.lang.ref.WeakReference
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Proxy

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/16
 *
 * 实现LifecycleEventObserver接口是为了监听宿主的生命周期
 * 实现KoinComponent是为了能在presenter中注入其它类,比如Model
 */
abstract class BaseMvpPresenter<V : BaseMvpView> : LifecycleEventObserver, KoinComponent {

    companion object {
        /**
         * 以下基本数据类型因为存在拆装箱操作,所以在invoke方法中不能直接返回null,某则会发生类型转换异常
         */
        private val defaultValue: Map<Class<*>, Any?> by lazy {
            mutableMapOf<Class<*>, Any?>().apply {
                put(Byte::class.java, Byte.MIN_VALUE)
                put(Short::class.java, Short.MIN_VALUE)
                put(Char::class.java, Char.MIN_VALUE)
                put(Int::class.java, Int.MIN_VALUE)
                put(Long::class.java, Long.MIN_VALUE)
                put(Float::class.java, Float.MIN_VALUE)
                put(Double::class.java, Double.MIN_VALUE)
                put(Boolean::class.java, false)
            }
        }

        private fun getDefaultReturnValue(method: Method): Any? {
            return defaultValue[method.returnType]
        }
    }

    /**
     * view的真实引用
     */
    private var viewRefs: WeakReference<V>? = null

    /**
     * view的代理实例
     */
    protected val view: V by lazy { createProxyView() }

    @Volatile
    private var compositeDisposable: CompositeDisposable? = null

    @Suppress("UNCHECKED_CAST")
    private fun findViewInterfaceClass(clazz: Class<*>): Class<V> {
        val type = clazz.genericSuperclass
            ?: throw IllegalAccessException("$clazz not found ParameterizedType for BaseMvpView")
        return if (type is ParameterizedType) {
            type.actualTypeArguments[0] as Class<V>
        } else {
            findViewInterfaceClass(type as Class<*>)
        }
    }

    private fun getViewInterfaceClass(): Class<V> {
        return findViewInterfaceClass(javaClass)
    }

    @Suppress("UNCHECKED_CAST")
    private fun createProxyView(): V {
        //通过presenter的父类获取view的接口类型
        val viewInterfaceClass = getViewInterfaceClass()
        return Proxy.newProxyInstance(
            javaClass.classLoader,
            arrayOf(viewInterfaceClass)
        ) invoke@{ _, method, args ->
            //如果method是定义在Object中的,那就执行常规调用
            if (method.declaringClass == Object::class.java) {
                return@invoke method.invoke(this, args)
            }
            val viewInstance = this.viewRefs?.get()
            if (viewInstance == null) {
                //view的真实引用被回收了(V层和P层解绑了或者界面被销毁了)
                Log.d("tag", "$this viewRefs is null")
                return@invoke getDefaultReturnValue(method)
            }
            if (!viewInstance.getLifecycle().currentState.isAtLeast(Lifecycle.State.CREATED)) {
                //view的生命周期不合法(未初始化完成或者已经被销毁)
                Log.d("tag", "$this lifecycle error")
                return@invoke getDefaultReturnValue(method)
            }
            try {
                //注意: 这里不能直接使用args,因为kotlin的数组类型和java的可变参不能通用
                return@invoke method.invoke(viewInstance, *(args ?: arrayOfNulls<Any?>(0)))
            } catch (t: Throwable) {
                Log.e("tag", "$this invoke $method error: $t")
            }
            return@invoke getDefaultReturnValue(method)
        } as V
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : BaseMvpView> attach(view: T) {
        //检查view的实例有没有实现presenter关联的接口,一般都是忘了写..
        val mvpView = view as? V ?: throw IllegalArgumentException(
            "$view not implements ${getViewInterfaceClass()}"
        )
        this.viewRefs = WeakReference(mvpView)
        mvpView.getLifecycle().addObserver(this)
        onAttached()
    }

    private fun detach() {
        viewRefs?.let {
            it.clear()
            viewRefs = null
        }
        clearDisposable()
        onDetached()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        onLifecycleStateChanged(event)
        when (event) {
            Lifecycle.Event.ON_START -> {
                //如果有使用EventBus,可以在这里注册
            }
            Lifecycle.Event.ON_STOP -> {
                //如果有使用EventBus,可以在这里反注册
            }
            Lifecycle.Event.ON_DESTROY -> {
                //自动解绑
                detach()
            }
            else -> {
                //no op
            }
        }
    }

    protected open fun onAttached() {
        //no op
    }

    protected open fun onDetached() {
        //no op
    }

    protected open fun onLifecycleStateChanged(event: Lifecycle.Event) {
        //no op
    }

    @Synchronized
    protected fun addDisposable(disposable: Disposable) {
        val compositeDisposable = compositeDisposable ?: CompositeDisposable().also {
            compositeDisposable = it
        }
        compositeDisposable.add(disposable)
    }

    @Synchronized
    protected fun clearDisposable() {
        compositeDisposable?.let {
            it.clear()
            compositeDisposable = null
        }
    }

}