package com.automizely.mvp.user.model

import io.reactivex.Single
import java.io.IOException

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/16
 */
class UserModel {

    fun loadUser(): Single<User> {
        return Single.create<User> { emitter ->
            Thread.sleep(2000)
            if (System.currentTimeMillis() % 2 == 0L) {
                emitter.onSuccess(User("111", "张三"))
            } else {
                emitter.onError(IOException("接口错误"))
            }
        }
    }

}