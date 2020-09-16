package com.automizely.mvp.user.presenter

import com.automizely.mvp.user.contract.UserContract
import com.automizely.mvp.user.model.UserModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.core.inject

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/16
 */
class UserPresenter : UserContract.AbsUserPresenter() {

    //通过注入依赖model
    private val userModel: UserModel by inject()

    override fun loadUser() {
        val disposable = userModel.loadUser()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ user ->
                view.onLoadUserSuccess(user)
            }, { t ->
                view.onLoadUserFail(t)
            })
        addDisposable(disposable)
    }

}