package com.automizely.framework.rx

import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/18
 */
abstract class AbsObserver<T> : Observer<T> {

    override fun onComplete() {

    }

    override fun onSubscribe(d: Disposable) {

    }

    override fun onNext(data: T) {

    }

    override fun onError(t: Throwable) {

    }
}