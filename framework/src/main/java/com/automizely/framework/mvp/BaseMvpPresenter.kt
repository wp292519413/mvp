package com.automizely.framework.mvp

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.automizely.framework.utils.EventBusUtil
import com.trello.rxlifecycle3.LifecycleProvider
import com.trello.rxlifecycle3.LifecycleTransformer
import com.trello.rxlifecycle3.RxLifecycle
import com.trello.rxlifecycle3.android.ActivityEvent
import com.trello.rxlifecycle3.android.RxLifecycleAndroid
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import org.koin.core.KoinComponent
import java.lang.ref.WeakReference
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Proxy

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/16
 *
 * 实现 LifecycleEventObserver 接口是为了监听宿主的生命周期
 * 实现 KoinComponent 是为了能在 presenter 中注入其它类,比如 model
 * 实现 LifecycleProvider 是为了在 presenter 中方便使用 rx
 */
abstract class BaseMvpPresenter<V : BaseMvpView> : LifecycleEventObserver, KoinComponent,
    LifecycleProvider<ActivityEvent> {

    companion object {
        /**
         * 以下基本数据类型因为存在拆装箱操作,所以在 invoke 方法中不能直接返回 null,某则会发生类型转换异常
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
                put(String::class.java, "")
            }
        }

        private fun getDefaultReturnValue(method: Method): Any? {
            return defaultValue[method.returnType]
        }
    }

    /**
     * view 的真实引用
     */
    private var realViewRefs: WeakReference<V>? = null

    /**
     * view 的代理实例
     */
    protected lateinit var view: V

    @Volatile
    private var compositeDisposable: CompositeDisposable? = null

    /**
     * 参考 rxlifecycle3: https://github.com/trello/RxLifecycle
     */
    private val lifecycleSubject = BehaviorSubject.create<ActivityEvent>()

    @Volatile
    private var scope: CoroutineScope? = null

    /**
     * presenter 的协程作用域,参考 viewModelScope
     */
    protected val presenterScope: CoroutineScope
        get() {
            return scope ?: synchronized(this) {
                scope ?: CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
            }
        }

    private val invocationHandler: InvocationHandler by lazy {
        object : InvocationHandler {
            override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
                //如果 method 是定义在 Object 中的,那就执行常规调用
                if (method.declaringClass == Object::class.java) {
                    return method.invoke(this, args)
                }
                val realView = realViewRefs?.get()
                //检查 view 的实例状态
                if (realView == null || !realView.isActive()) {
                    Log.w("tag", "$method view state invalid")
                    return getDefaultReturnValue(method)
                }
                return try {
                    //注意: 这里不能直接使用 args,因为 kotlin 的数组类型和 java 的可变参不能通用
                    method.invoke(realView, *(args ?: arrayOfNulls<Any?>(0)))
                } catch (t: Throwable) {
                    Log.e("tag", "$method error: $t")
                    getDefaultReturnValue(method)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : BaseMvpView> attach(view: T) {
        //从 presenter 的类定义中获取 view 的类型
        val viewClass = findViewClassFromPresenterClass()
        //检查 view 的实例有没有实现 presenter 定义的 view 接口
        if (!viewClass.isAssignableFrom(view.javaClass)) {
            throw IllegalArgumentException("$view not implements $viewClass")
        }
        val realView = view as V
        //保存 view 的引用
        this.realViewRefs = WeakReference(realView)
        //创建 view 的代理对象
        this.view = Proxy.newProxyInstance(viewClass.classLoader, arrayOf(viewClass), invocationHandler) as V
        //监听 view 的生命周期
        realView.getLifecycle().addObserver(this)
        onAttached()
    }

    @Suppress("UNCHECKED_CAST")
    private fun findViewClassFromPresenterClass(): Class<V> {
        var absPresenterClass = this.javaClass.superclass!!
        while (absPresenterClass.superclass != BaseMvpPresenter::class.java) {
            absPresenterClass = absPresenterClass.superclass!!
        }
        val type = absPresenterClass.genericSuperclass as ParameterizedType
        return type.actualTypeArguments[0] as Class<V>
    }

    private fun detach() {
        realViewRefs?.get()?.let {
            it.getLifecycle().removeObserver(this)
            realViewRefs?.clear()
            realViewRefs = null
        }
        clearDisposable()
        cancelCoroutineJob()
        onDetached()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                lifecycleSubject.onNext(ActivityEvent.CREATE)
            }
            Lifecycle.Event.ON_START -> {
                lifecycleSubject.onNext(ActivityEvent.START)
                if (isEventBusEnabled()) {
                    EventBusUtil.register(this)
                }
            }
            Lifecycle.Event.ON_RESUME -> {
                lifecycleSubject.onNext(ActivityEvent.RESUME)
            }
            Lifecycle.Event.ON_PAUSE -> {
                lifecycleSubject.onNext(ActivityEvent.PAUSE)
            }
            Lifecycle.Event.ON_STOP -> {
                lifecycleSubject.onNext(ActivityEvent.STOP)
                if (isEventBusEnabled()) {
                    EventBusUtil.unregister(this)
                }
            }
            Lifecycle.Event.ON_DESTROY -> {
                lifecycleSubject.onNext(ActivityEvent.DESTROY)
                detach()
            }
            Lifecycle.Event.ON_ANY -> {
                //no op
            }
        }
        //回调到业务层
        onLifecycleStateChanged(event)
    }

    /**
     * 是否开启EventBus支持,默认关闭
     */
    protected open fun isEventBusEnabled(): Boolean {
        return false
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

    /**
     * 取消协程调度
     */
    private fun cancelCoroutineJob() {
        val scope = this.scope ?: return
        val job = scope.coroutineContext[Job]
        if (job == null) {
            Log.e("tag", "Scope cannot be cancelled because it does not have a job: $scope")
        } else {
            job.cancel(null)
        }
    }

    /**
     * rxlifecycle
     */
    override fun lifecycle(): Observable<ActivityEvent> {
        return lifecycleSubject.hide()
    }

    override fun <T> bindUntilEvent(event: ActivityEvent): LifecycleTransformer<T> {
        return RxLifecycle.bindUntilEvent(lifecycleSubject, event)
    }

    override fun <T> bindToLifecycle(): LifecycleTransformer<T> {
        return RxLifecycleAndroid.bindActivity(lifecycleSubject)
    }

    fun <T> bindUntilDetach(): LifecycleTransformer<T> {
        return RxLifecycle.bindUntilEvent(lifecycleSubject, ActivityEvent.DESTROY)
    }

}