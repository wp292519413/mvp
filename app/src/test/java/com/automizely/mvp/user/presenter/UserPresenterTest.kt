package com.automizely.mvp.user.presenter

import com.automizely.mvp.base.BaseUnitTest
import com.automizely.mvp.base.mockMvpView
import com.automizely.mvp.user.contract.UserContract
import com.automizely.mvp.user.model.User
import com.automizely.mvp.user.model.UserModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.reactivex.Single
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTestRule

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2021/1/5
 *
 * UserPresenter 单测
 */
class UserPresenterTest : BaseUnitTest() {

    /**
     * 创建 mock 的 view 实例
     */
    private val mockUserView: UserContract.IUserView = mockMvpView()

    /**
     * 创建 mock 的 model 实例
     */
    private val mockUserModel: UserModel = mockk()

    /**
     * 设置 koin 注入我们自定义的 mock 实例
     */
    private val testUserModule = module {
        single { mockUserModel }
    }

    /**
     * 启动 koin 的单测模式(在执行单测前自动调用 startKoin(),结束后自动调用 stopKoin())
     */
    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(testUserModule)
    }

    @Test
    fun testLogin() {
        //初始化 presenter
        val userPresenter = UserPresenter()
        userPresenter.attach(mockUserView)

        //创建 mock user 结果
        val mockUser = spyk(objToCopy = User("111", "laowang"))

        val loginSource = Single.create<User> { emitter ->
            //emitter.onError(IOException("自定义错误"))
            emitter.onSuccess(mockUser)
        }
        //mock login 方法的返回值
        every { mockUserModel.login(any(), any()) } returns loginSource

        //mock 参数
        val name = "123"
        val pwd = "123"

        //调用测试方法
        userPresenter.login(name, pwd)

        //验证 view 执行了对应的方法
        //verify { mockUserView.onLoginFail("自定义错误") }
        verify { mockUserView.onLoginSuccess(mockUser) }
    }
}