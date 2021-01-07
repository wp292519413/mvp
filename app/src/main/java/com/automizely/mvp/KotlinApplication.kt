package com.automizely.mvp

import android.app.Application
import android.util.Log
import com.automizely.login.di.loginModule
import com.automizely.mvp.di.appModule
import io.reactivex.plugins.RxJavaPlugins
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/16
 */
class KotlinApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()
        setRxJavaErrorHandler()
    }

    private fun initKoin() {
        startKoin {
            //日志
            androidLogger()
            //将application注入到koin中
            androidContext(this@KotlinApplication)
            //注册多个module,支持多个模块
            modules(appModule + loginModule)
        }
    }

    /**
     * RxJava当取消订阅后(dispose())抛出的异常后续无法接收(此时后台线程仍在跑,可能会抛出IO等异常)
     * 设置该情况下的异常全部由RxJavaPlugin接收
     */
    private fun setRxJavaErrorHandler() {
        RxJavaPlugins.setErrorHandler { t ->
            Log.d("tag", "RxJavaPlugins ErrorHandler: $t")
        }
    }

}