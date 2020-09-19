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
 * 实现LifecycleProvider是为了在presenter中方便使用rx
 */
abstract class BaseMvpPresenter<V : BaseMvpView> : LifecycleEventObserver, KoinComponent,
    LifecycleProvider<ActivityEvent> {

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

    /**
     * 参考rxlifecycle3: https://github.com/trello/RxLifecycle
     */
    private val lifecycleSubject = BehaviorSubject.create<ActivityEvent>()

    @Volatile
    private var scope: CoroutineScope? = null

    /**
     * presenter的协程作用域,参考viewModelScope
     */
    protected val presenterScope: CoroutineScope
        get() {
            return scope ?: synchronized(this) {
                scope ?: CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
            }
        }

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

    private fun getRealView(): V? {
        return viewRefs?.get()
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
            val realView = getRealView()
            if (realView == null) {
                //view的真实引用被回收了(V层和P层解绑了或者界面被销毁了)
                Log.w("tag", "$method realView is null")
                return@invoke getDefaultReturnValue(method)
            }
            if (!isViewActive()) {
                //view的生命周期不合法
                Log.w("tag", "$method view state invalid")
                return@invoke getDefaultReturnValue(method)
            }
            try {
                //注意: 这里不能直接使用args,因为kotlin的数组类型和java的可变参不能通用
                return@invoke method.invoke(realView, *(args ?: arrayOfNulls<Any?>(0)))
            } catch (t: Throwable) {
                Log.e("tag", "$method error: $t")
            }
            return@invoke getDefaultReturnValue(method)
        } as V
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : BaseMvpView> attach(view: T) {
        //检查view的实例有没有实现presenter关联的接口,一般都是忘了写..
        val realView = view as? V ?: throw IllegalArgumentException(
            "$view not implements ${getViewInterfaceClass()}"
        )
        this.viewRefs = WeakReference(realView)
        realView.getLifecycle().addObserver(this)
        onAttached()
    }

    private fun detach() {
        getLifecycle()?.removeObserver(this)
        viewRefs?.let {
            it.clear()
            viewRefs = null
        }
        clearDisposable()
        cancelCoroutineJob()
        onDetached()
    }

    protected fun getLifecycle(): Lifecycle? {
        return viewRefs?.get()?.getLifecycle()
    }

    protected fun isViewActive(): Boolean {
        return getLifecycle()?.currentState?.isAtLeast(Lifecycle.State.CREATED) ?: false
    }

    protected fun getCurrentState(): Lifecycle.State {
        return getLifecycle()?.currentState ?: Lifecycle.State.DESTROYED
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        Log.d("tag", "onStateChanged: $this $event")
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