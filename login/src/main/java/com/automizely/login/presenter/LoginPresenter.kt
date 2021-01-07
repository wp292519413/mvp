package com.automizely.login.presenter

import com.automizely.login.contract.LoginContract
import com.automizely.login.model.LoginModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.core.component.inject

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/16
 */
class LoginPresenter : LoginContract.AbsLoginPresenter() {

    private val loginModel: LoginModel by inject()

    override fun login(name: String, pwd: String) {
        val disposable = loginModel.login(name, pwd)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ token ->
                view.onLoginSuccess(token)
            }, { t ->
                view.onLoginFail(t)
            })
        addDisposable(disposable)
    }

}