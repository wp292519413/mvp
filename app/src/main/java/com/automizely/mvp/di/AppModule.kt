package com.automizely.mvp.di

import com.automizely.mvp.java.di.JavaDemoModule
import com.automizely.mvp.java.presenter.JavaDemoPresenter
import com.automizely.mvp.user.model.UserModel
import com.automizely.mvp.user.presenter.UserPresenter
import com.automizely.mvp.user.presenter.UserPresenter2
import org.koin.dsl.module

//user 模块
val userModule = module {
    factory { UserPresenter() }
    factory { UserPresenter2() }
    //model一般可以指定为单例的
    single { UserModel() }
}

//java demo 模块
val javaDemoModule = module {
    factory { JavaDemoPresenter() }
}

val appModule = listOf(
    //user
    userModule,
    //java demo
    //javaDemoModule,
    JavaDemoModule.javaDemoModule
)

