package com.automizely.framework.mvp

import android.content.Context
import com.trello.rxlifecycle3.components.support.RxFragment

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/17
 */
abstract class BaseMvpFragment : RxFragment(), BaseMvpView {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        attachPresenter()
    }

}