package com.automizely.mvp.user.model

import io.reactivex.Observable
import java.io.IOException

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/16
 */
class UserModel {

    fun loadUser(): Observable<User> {
        return Observable.create<User> { emitter ->
            Thread.sleep(2000)
            if (System.currentTimeMillis() % 2 == 0L) {
                emitter.onNext(User("111", "张三"))
            } else {
                emitter.onError(IOException("接口错误"))
            }
        }
    }

}