package com.automizely.framework.mvp

import androidx.lifecycle.Lifecycle

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/16
 */
interface BaseMvpView {

    fun getLifecycle(): Lifecycle

}