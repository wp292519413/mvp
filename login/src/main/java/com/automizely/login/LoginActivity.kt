package com.automizely.login

import android.os.Bundle
import android.util.Log
import com.automizely.framework.base.BaseMvpActivity
import com.automizely.login.contract.LoginContract
import com.automizely.login.databinding.LayoutActivityLoginBinding
import com.automizely.login.presenter.LoginPresenter
import org.koin.android.ext.android.inject

class LoginActivity : BaseMvpActivity(), LoginContract.ILoginView {

    private val viewBinding: LayoutActivityLoginBinding by lazy {
        LayoutActivityLoginBinding.inflate(layoutInflater)
    }
    private val loginPresenter: LoginPresenter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.run {
            btnLogin.setOnClickListener {
                login()
            }
        }
    }

    private fun login() {
        viewBinding.tvTest.text = "登录中.."
        loginPresenter.login("laowang", "123")
    }

    override fun onLoginSuccess(token: String) {
        Log.e("tag", "onLoginSuccess: $token")
        viewBinding.tvTest.text = token
    }

    override fun onLoginFail(t: Throwable) {
        Log.e("tag", "onLoginFail: $t")
        viewBinding.tvTest.text = "登录失败"
    }
}
