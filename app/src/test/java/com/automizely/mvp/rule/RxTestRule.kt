package com.automizely.mvp.rule

import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2021/1/5
 */
class RxTestRule : TestRule {

    override fun apply(base: Statement, description: Description?): Statement {
        return object : Statement() {
            override fun evaluate() {
                //还原设置
                RxJavaPlugins.reset()
                RxAndroidPlugins.reset()
                //设置 io 线程为测试线程
                RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
                //设置 newThread 线程为测试线程
                RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
                //设置 computation 线程为测试线程
                RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
                //设置 mainThread 线程为测试线程
                RxAndroidPlugins.setMainThreadSchedulerHandler { Schedulers.trampoline() }
                //设置 mainThread 线程为测试线程
                RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
                //执行测试代码
                try {
                    base.evaluate()
                } finally {
                    //执行完之后还原设置
                    RxJavaPlugins.reset()
                    RxAndroidPlugins.reset()
                }
            }
        }
    }

}