package com.automizely.framework.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/16
 */
abstract class BaseMvpActivity : AppCompatActivity(), BaseMvpView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachPresenter()
    }

}