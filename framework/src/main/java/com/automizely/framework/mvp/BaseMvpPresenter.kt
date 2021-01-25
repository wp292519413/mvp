package com.automizely.framework.mvp

import android.util.Log
import androidx.annotation.CallSuper
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
import org.koin.core.Koin
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.context.GlobalContext
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
@OptIn(KoinApiExtension::class)
abstract class BaseMvpPresenter<V : BaseMvpView> : LifecycleEventObserver, KoinComponent,
    LifecycleProvider<ActivityEvent> {

    /**
     * view 的真实引用
     */
    private var viewRefs: WeakReference<V>? = null

    /**
     * view 的代理实例,业务层访问的都是代理 view 的实例
     */
    protected val view: V by lazy { createProxyView() }

    /**
     * EventBus, 默认关闭
     */
    protected open val eventBusState = EventBusState.DISABLE

    /**
     * rx lifecycle
     * #addDisposable()
     * #disposeAll()
     */
    private val compositeDisposable = CompositeDisposable()

    /**
     * 参考 rxlifecycle3: https://github.com/trello/RxLifecycle
     */
    private val lifecycleSubject = BehaviorSubject.create<ActivityEvent>()

    @Volatile
    private var coroutineScope: CoroutineScope? = null

    /**
     * presenter 的协程作用域,参考 viewModelScope
     */
    protected val presenterScope: CoroutineScope
        get() {
            return coroutineScope ?: synchronized(this) {
                coroutineScope ?: CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
            }
        }

    /**
     * 用于判断 presenter 的生命周期是否处于活跃状态
     */
    fun isActive(): Boolean = viewRefs?.get()?.isActive() ?: false

    /**
     * 获取 presenter 的当前生命周期状态
     */
    fun getCurrentState(): Lifecycle.State = viewRefs?.get()?.getCurrentState() ?: Lifecycle.State.DESTROYED

    fun attach(view: V) {
        viewRefs = WeakReference(view)
        view.getLifecycle().addObserver(this)
        onAttached()
    }

    protected open fun onAttached() {
        //no op
    }

    @Suppress("UNCHECKED_CAST")
    private fun createProxyView(): V {
        val viewClass = findViewClassFromPresenterClass()
        return Proxy.newProxyInstance(viewClass.classLoader, arrayOf(viewClass), object : InvocationHandler {
            override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
                //如果 method 是定义在 Object 中的,那就执行常规调用
                if (method.declaringClass == Object::class.java) {
                    return method.invoke(this, args)
                }
                val view = viewRefs?.get()
                //检查 view 的实例状态
                if (view == null || !view.isActive()) {
                    Log.w("tag", "$method view state invalid")
                    return getDefaultReturnValue(method)
                }
                return try {
                    //注意: 这里不能直接使用 args,因为 kotlin 的数组类型和 java 的可变参不能通用
                    method.invoke(view, *(args ?: arrayOfNulls<Any?>(0)))
                } catch (t: Throwable) {
                    Log.e("tag", "$method error: $t")
                    getDefaultReturnValue(method)
                }
            }
        }) as V
    }

    @Suppress("UNCHECKED_CAST")
    private fun findViewClassFromPresenterClass(): Class<V> {
        var absPresenterClass = javaClass.superclass
            ?: throw IllegalArgumentException("can not found view interface: $this")
        while (absPresenterClass.superclass != BaseMvpPresenter::class.java) {
            absPresenterClass = absPresenterClass.superclass
                ?: throw IllegalArgumentException("can not found view interface: $this")
        }
        val type = absPresenterClass.genericSuperclass as ParameterizedType
        return type.actualTypeArguments[0] as? Class<V>
            ?: throw IllegalArgumentException("can not found view interface: $this")
    }

    private fun detach() {
        viewRefs?.let {
            it.get()?.getLifecycle()?.removeObserver(this)
            it.clear()
            viewRefs = null
        }
        disposeAll()
        cancelCoroutineJob()
        onDetached()
    }

    protected open fun onDetached() {
        //no op
    }

    @CallSuper
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                lifecycleSubject.onNext(ActivityEvent.CREATE)
                if (eventBusState == EventBusState.CREATE_DESTROY) {
                    EventBusUtil.register(this)
                }
            }
            Lifecycle.Event.ON_START -> {
                lifecycleSubject.onNext(ActivityEvent.START)
                if (eventBusState == EventBusState.START_STOP) {
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
                if (eventBusState == EventBusState.START_STOP) {
                    EventBusUtil.unregister(this)
                }
            }
            Lifecycle.Event.ON_DESTROY -> {
                lifecycleSubject.onNext(ActivityEvent.DESTROY)
                if (eventBusState == EventBusState.CREATE_DESTROY) {
                    EventBusUtil.unregister(this)
                }
                detach()
            }
            Lifecycle.Event.ON_ANY -> {
                //no op
            }
        }
    }

    enum class EventBusState {
        /**
         * 不启用 EventBus
         */
        DISABLE,

        /**
         * EventBus 在 onCreate 中注册, onDestroy 中反注册
         */
        CREATE_DESTROY,

        /**
         * EventBus 在 onStop 中注册, onStop 中反注册 (官方推荐)
         */
        START_STOP
    }

    /**
     * 复写 KoinComponent 的 getKoin() 方法, 避免 Java 编写的 presenter 需要实现该方法
     */
    override fun getKoin(): Koin {
        return GlobalContext.get()
    }

    protected fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    protected fun disposeAll() {
        compositeDisposable.dispose()
    }

    /**
     * 取消协程调度
     */
    private fun cancelCoroutineJob() {
        val scope = this.coroutineScope ?: return
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

    companion object {
        /**
         * 以下基本数据类型因为存在拆装箱操作,所以在 invoke 方法中不能直接返回 null,某则会发生类型转换异常
         */
        private val defaultValue: Map<Class<*>, Any> by lazy {
            mutableMapOf<Class<*>, Any>().apply {
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

}