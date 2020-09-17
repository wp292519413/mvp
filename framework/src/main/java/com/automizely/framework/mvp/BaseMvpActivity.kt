package com.automizely.framework.mvp

import android.os.Bundle
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/16
 */
abstract class BaseMvpActivity : RxAppCompatActivity(), BaseMvpView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachPresenter()
    }

}