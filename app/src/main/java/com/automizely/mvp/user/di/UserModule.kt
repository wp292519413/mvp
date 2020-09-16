package com.automizely.mvp.user.di

import com.automizely.mvp.user.model.UserModel
import com.automizely.mvp.user.presenter.UserPresenter
import org.koin.dsl.module

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/16
 */
val userModule = module {
    //model一般可以指定为单例的
    single { UserModel() }
    factory { UserPresenter() }
}