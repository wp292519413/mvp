package com.automizely.mvp.user.model

import io.reactivex.Single
import java.io.IOException

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/16
 */
class UserModel {

    fun login(name: String, pwd: String): Single<User> {
        return Single.create { emitter ->
            Thread.sleep(3000)
            if (name == "zhangsan" && pwd == "123") {
                emitter.onSuccess(User("111", "张三"))
            } else {
                emitter.onError(IOException("用户名或者密码错误"))
            }
        }
    }

}