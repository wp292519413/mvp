package com.automizely.mvp.user.contract

import com.automizely.framework.base.BaseMvpPresenter
import com.automizely.framework.base.BaseMvpView

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/16
 */
interface UserContract2 {

    interface IUserView2 : BaseMvpView {

    }

    abstract class AbsUserPresenter2 : BaseMvpPresenter<IUserView2>() {

        abstract fun loadUser()

    }

}