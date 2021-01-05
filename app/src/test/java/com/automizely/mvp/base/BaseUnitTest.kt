package com.automizely.mvp.base

import com.automizely.mvp.rule.AndroidLogTestRule
import com.automizely.mvp.rule.RxTestRule
import org.junit.Rule
import org.koin.test.KoinTest

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2021/1/5
 */
abstract class BaseUnitTest : KoinTest {

    /**
     * mock android.util.Log 类相关的方法
     */
    @get:Rule
    val androidLogTestRule = AndroidLogTestRule()

    /**
     * 让 rx 的异步变为同步
     */
    @get:Rule
    val rxTestRule = RxTestRule()

}