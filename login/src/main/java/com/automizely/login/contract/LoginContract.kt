package com.automizely.login.contract

import com.automizely.framework.mvp.BaseMvpPresenter
import com.automizely.framework.mvp.BaseMvpView

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/16
 */
interface LoginContract {

    interface ILoginView : BaseMvpView {

        fun onLoginSuccess(token: String)

        fun onLoginFail(t: Throwable)

    }

    abstract class AbsLoginPresenter : BaseMvpPresenter<ILoginView>() {

        abstract fun login(name: String, pwd: String)

    }

}