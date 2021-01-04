package com.automizely.login

import android.os.Bundle
import com.automizely.framework.mvp.BaseMvpActivity
import com.automizely.framework.mvp.injectPresenter
import com.automizely.login.contract.LoginContract
import com.automizely.login.databinding.LayoutActivityLoginBinding
import com.automizely.login.presenter.LoginPresenter

class LoginActivity : BaseMvpActivity(), LoginContract.ILoginView {

    private val viewBinding: LayoutActivityLoginBinding by lazy {
        LayoutActivityLoginBinding.inflate(layoutInflater)
    }

    //使用 koin 注入 presenter 并自动和 V 层绑定
    private val loginPresenter: LoginPresenter by injectPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.run {
            btnLogin.setOnClickListener { login() }
        }
    }

    private fun login() {
        viewBinding.tvTest.text = "登录中.."
        loginPresenter.login("laowang", "123")
    }

    override fun onLoginSuccess(token: String) {
        viewBinding.tvTest.text = token
    }

    override fun onLoginFail(t: Throwable) {
        viewBinding.tvTest.text = "登录失败"
    }
}
