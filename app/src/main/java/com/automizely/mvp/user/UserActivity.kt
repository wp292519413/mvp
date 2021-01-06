package com.automizely.mvp.user

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import com.automizely.framework.mvp.BaseMvpActivity
import com.automizely.framework.mvp.getPresenter
import com.automizely.framework.mvp.injectPresenter
import com.automizely.mvp.databinding.LayoutActivityUserBinding
import com.automizely.mvp.java.JavaDemoActivity
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

    //懒加载形式注入 presenter
    private val userPresenter: UserPresenter by injectPresenter()

    //同步获取 presenter
    private val userPresenter2: UserPresenter2 = getPresenter()

    private var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.run {
            btnLogin.isEnabled = false
            btnLogin2.isEnabled = false
            etName.doOnTextChanged { _, _, _, _ ->
                btnLogin.isEnabled = !etName.isEmpty() && !etPwd.isEmpty()
                btnLogin2.isEnabled = !etName.isEmpty() && !etPwd.isEmpty()
            }
            etPwd.doOnTextChanged { _, _, _, _ ->
                btnLogin.isEnabled = !etName.isEmpty() && !etPwd.isEmpty()
                btnLogin2.isEnabled = !etName.isEmpty() && !etPwd.isEmpty()
            }
            btnLogin.setOnClickListener {
                showLoading("登录中..")
                userPresenter.login(etName.getString(), etPwd.getString())
            }
            btnLogin2.setOnClickListener {
                showLoading("登录中..")
                userPresenter2.login2(etName.getString(), etPwd.getString())
            }
            btnJava.setOnClickListener {
                //跳转 Java demo activity
                startActivity(Intent(this@UserActivity, JavaDemoActivity::class.java))
            }
        }
    }

    private fun EditText.getString(): String {
        return text?.toString()?.trim() ?: ""
    }

    private fun EditText.isEmpty(): Boolean {
        return getString().isEmpty()
    }

    private fun showLoading(msg: String) {
        dialog = AlertDialog.Builder(this)
            .setMessage(msg)
            .show()
    }

    private fun dismissLoading() {
        dialog?.dismiss()
        dialog = null
    }

    override fun onLoginSuccess(user: User) {
        dismissLoading()
        viewBinding.etPwd.run {
            setText("")
            setSelection(0)
        }
        Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
    }

    override fun onLoginFail(msg: String) {
        dismissLoading()
        viewBinding.etPwd.run {
            setText("")
            setSelection(0)
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onLoginSuccess2(user: User) {
        dismissLoading()
        viewBinding.etPwd.run {
            setText("")
            setSelection(0)
        }
        Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
    }

    override fun onLoginFail2(msg: String) {
        dismissLoading()
        viewBinding.etPwd.run {
            setText("")
            setSelection(0)
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}