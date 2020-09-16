package com.automizely.mvp.di

import com.automizely.mvp.home.di.homeModule
import com.automizely.mvp.user.di.userModule

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/16
 *
 * app模块所有的注入放在这(个人觉得每个不同的小模块分开放会比较好)
 */
val appModule = listOf(
    //user
    userModule,
    //home
    homeModule
)