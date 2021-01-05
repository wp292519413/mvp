package com.automizely.mvp.user.contract

import com.automizely.framework.mvp.BaseMvpPresenter
import com.automizely.framework.mvp.BaseMvpView
import com.automizely.mvp.user.model.User

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/16
 */
interface UserContract2 {

    interface IUserView2 : BaseMvpView {

        fun onLoginSuccess2(user: User)

        fun onLoginFail2(msg: String)

    }

    abstract class AbsUserPresenter2 : BaseMvpPresenter<IUserView2>() {

        abstract fun login2(name: String, pwd: String)

    }

}