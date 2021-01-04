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

        fun onLoadUser2Success(user: User)

        fun onLoadUser2Fail(t: Throwable)

    }

    abstract class AbsUserPresenter2 : BaseMvpPresenter<IUserView2>() {

        abstract fun loadUser()

    }

}