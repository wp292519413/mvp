package com.automizely.mvp.user

import android.os.Bundle
import com.automizely.framework.base.BaseMvpActivity
import com.automizely.mvp.MyApplication
import com.automizely.mvp.databinding.LayoutActivityUserBinding
import com.automizely.mvp.user.contract.UserContract
import com.automizely.mvp.user.model.User
import com.automizely.mvp.user.presenter.UserPresenter
import org.koin.android.ext.android.inject

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/16
 */
class UserActivity : BaseMvpActivity(), UserContract.IUserView {

    private val viewBinding: LayoutActivityUserBinding by lazy {
        LayoutActivityUserBinding.inflate(layoutInflater)
    }

    //通过注入依赖presenter
    private val userPresenter: UserPresenter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.run {
            btnTest.setOnClickListener {
                loadUsers()
            }
            btnLogin.setOnClickListener {
                toLogin()
            }
        }
    }

    private fun toLogin() {
        //todo 简单写个跨模块的调用
        (application as MyApplication).startLoginActivity(this)
    }

    private fun loadUsers() {
        viewBinding.tvTest.text = "加载中.."
        userPresenter.loadUser()
    }

    override fun onLoadUserSuccess(user: User) {
        viewBinding.tvTest.text = user.toString()
    }

    override fun onLoadUserFail(t: Throwable) {
        viewBinding.tvTest.text = t.message
    }

}