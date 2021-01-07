package com.automizely.mvp.user.presenter

import com.automizely.mvp.user.contract.UserContract2
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
class UserPresenter2 : UserContract2.AbsUserPresenter2() {

    //通过注入依赖model
    private val userModel: UserModel by inject()

    override fun login2(name: String, pwd: String) {
        if (name.isEmpty() || pwd.isEmpty()) {
            view.onLoginFail2("用户名或者密码不能为空")
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
                    view.onLoginSuccess2(user)
                }

                override fun onError(t: Throwable) {
                    view.onLoginFail2(t.message ?: "未知错误")
                }
            })
    }

}