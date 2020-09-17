package com.automizely.framework.base

import android.content.Context
import androidx.fragment.app.Fragment

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/17
 */
abstract class BaseMvpFragment : Fragment(), BaseMvpView {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        attachPresenter()
    }

}