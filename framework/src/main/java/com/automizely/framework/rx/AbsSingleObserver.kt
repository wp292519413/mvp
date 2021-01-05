package com.automizely.framework.rx

import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/18
 */
abstract class AbsSingleObserver<T> : SingleObserver<T> {

    override fun onSubscribe(d: Disposable) {

    }
}