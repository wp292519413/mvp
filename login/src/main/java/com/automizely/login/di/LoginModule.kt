package com.automizely.login.di

import com.automizely.login.model.LoginModel
import com.automizely.login.presenter.LoginPresenter
import org.koin.dsl.module

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/16
 */
val loginModule = module {
    factory { LoginPresenter() }
    single { LoginModel() }
}