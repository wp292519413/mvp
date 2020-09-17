package com.automizely.mvp.user.contract

import com.automizely.framework.mvp.BaseMvpPresenter
import com.automizely.framework.mvp.BaseMvpView
import com.automizely.mvp.user.model.User

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/16
 */
interface UserContract {

    interface IUserView : BaseMvpView {

        fun onLoadUserSuccess(user: User)

        fun onLoadUserFail(t: Throwable)

    }

    abstract class AbsUserPresenter : BaseMvpPresenter<IUserView>() {

        abstract fun loadUser()

    }

}