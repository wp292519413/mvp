package com.automizely.mvp.base

import com.automizely.framework.mvp.BaseMvpView
import io.mockk.every
import io.mockk.mockk

/**
 * 返回一个 mock BaseMvpView 的实例
 */
inline fun <reified T : BaseMvpView> mockMvpView(): T {
    val mockView = mockk<T>()
    every { mockView.getLifecycle() } returns TestLifecycle()
    every { mockView.isActive() } returns true
    return mockView
}