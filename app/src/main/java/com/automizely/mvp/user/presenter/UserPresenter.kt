package com.automizely.mvp.user.presenter

import com.automizely.mvp.user.contract.UserContract
import com.automizely.mvp.user.model.User
import com.automizely.mvp.user.model.UserModel
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.core.component.inject

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/16
 */
class UserPresenter : UserContract.AbsUserPresenter() {

    //通过注入依赖model
    private val userModel: UserModel by inject()

    override fun login(name: String, pwd: String) {
        if (name.isEmpty() || pwd.isEmpty()) {
            view.onLoginFail("用户名或者密码不能为空")
            return
        }
        userModel.login(name, pwd)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<User> {
                override fun onSubscribe(d: Disposable) {
                    addDisposable(d)
                }

                override fun onSuccess(user: User) {
                    view.onLoginSuccess(user)
                }

                override fun onError(t: Throwable) {
                    view.onLoginFail(t.message ?: "未知错误")
                }
            })
    }

}