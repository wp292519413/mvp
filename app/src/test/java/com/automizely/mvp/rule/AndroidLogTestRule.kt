package com.automizely.mvp.rule

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2021/1/5
 *
 * mock android.util.Log 类相关的方法
 */
class AndroidLogTestRule : TestRule {

    override fun apply(base: Statement, description: Description?): Statement {
        return object : Statement() {
            override fun evaluate() {
                mockAndroidLog()
                base.evaluate()
            }
        }
    }

    private fun mockAndroidLog() {
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
    }

}