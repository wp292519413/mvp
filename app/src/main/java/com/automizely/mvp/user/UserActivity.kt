package com.automizely.mvp.user

import android.content.Intent
import android.os.Bundle
import com.automizely.framework.mvp.BaseMvpActivity
import com.automizely.framework.mvp.injectPresenter
import com.automizely.login.LoginActivity
import com.automizely.mvp.databinding.LayoutActivityUserBinding
import com.automizely.mvp.user.contract.UserContract
import com.automizely.mvp.user.contract.UserContract2
import com.automizely.mvp.user.model.User
import com.automizely.mvp.user.presenter.UserPresenter
import com.automizely.mvp.user.presenter.UserPresenter2

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/16
 */
class UserActivity : BaseMvpActivity(), UserContract.IUserView, UserContract2.IUserView2 {

    private val viewBinding: LayoutActivityUserBinding by lazy {
        LayoutActivityUserBinding.inflate(layoutInflater)
    }

    //使用 koin 注入 presenter 并自动和 V 层绑定
    private val userPresenter: UserPresenter by injectPresenter()

    //使用 koin 注入 presenter 并自动和 V 层绑定
    private val userPresenter2: UserPresenter2 by injectPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.run {
            btnTest.setOnClickListener { loadUsers() }
            btnLogin.setOnClickListener { toLogin() }
        }
    }

    private fun toLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun loadUsers() {
        viewBinding.tvTest.text = "加载中.."
        viewBinding.tvTest2.text = "加载中.."
        userPresenter.loadUser()
        userPresenter2.loadUser()
    }

    override fun onLoadUserSuccess(user: User) {
        viewBinding.tvTest.text = user.toString()
    }

    override fun onLoadUserFail(t: Throwable) {
        viewBinding.tvTest.text = t.message
    }

    override fun onLoadUser2Success(user: User) {
        viewBinding.tvTest2.text = user.toString()
    }

    override fun onLoadUser2Fail(t: Throwable) {
        viewBinding.tvTest2.text = t.message
    }

}