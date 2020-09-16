package com.automizely.framework.base

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.lang.reflect.Field

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

    private fun attachPresenter() {
        val t1 = System.currentTimeMillis()
        //java反射注册-模拟器冷启动50ms左右
        //注意:这里的耗时主要是花在了初始化lazy属性上,和反射本身无关
        javaClass.declaredFields.forEach {
            (getFieldValue(it) as? BaseMvpPresenter<*>)?.attach(this)
        }
        //kotlin反射注册-模拟器冷启动2000ms左右,kotlin的反射真可怕
//        javaClass.kotlin.declaredMemberProperties.forEach {
//            it.isAccessible = true
//            (it.get(this) as? BaseMvpPresenter<*>)?.attach(this)
//        }
        Log.e("tag", "attachPresenter useTime: ${System.currentTimeMillis() - t1}")
    }

    private fun getFieldValue(field: Field): Any? {
        field.isAccessible = true
        val value = field.get(this)
        return if (value is Lazy<*>) {
            value.value
        } else {
            value
        }
    }

    private fun isPresenterType(clazz: Class<*>): Boolean {
        val superclass = clazz.superclass ?: return false
        Log.e("tag", "superclass: $superclass")
        return if (superclass == BaseMvpPresenter::class.java) {
            true
        } else {
            isPresenterType(superclass)
        }
    }

}