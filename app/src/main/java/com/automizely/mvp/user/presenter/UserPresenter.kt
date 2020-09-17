package com.automizely.mvp.user.presenter

import com.automizely.framework.rx.AbsSingleObserver
import com.automizely.mvp.user.contract.UserContract
import com.automizely.mvp.user.model.User
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
        userModel.loadUser()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindUntilDetach())
            .subscribe(object : AbsSingleObserver<User>() {
                override fun onSuccess(data: User) {
                    view.onLoadUserSuccess(data)
                }

                override fun onError(t: Throwable) {
                    view.onLoadUserFail(t)
                }
            })
    }

}