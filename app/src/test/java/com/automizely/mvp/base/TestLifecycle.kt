package com.automizely.mvp.base

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2021/1/5
 */
class TestLifecycle : Lifecycle() {

    override fun addObserver(observer: LifecycleObserver) {
        //no op
    }

    override fun removeObserver(observer: LifecycleObserver) {
        //no op
    }

    override fun getCurrentState(): State {
        return State.RESUMED
    }
}