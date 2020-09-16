package com.automizely.login.model

import io.reactivex.Observable

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/16
 */
class LoginModel {

    fun login(name: String, pwd: String): Observable<String> {
        return Observable.create { emitter ->
            Thread.sleep(2000)
            emitter.onNext("token: $name-$pwd")
        }
    }

}