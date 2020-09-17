package com.automizely.framework.base

import android.util.Log
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * 将presenter与view层绑定
 */
internal fun BaseMvpView.attachPresenter() {
    val t1 = System.currentTimeMillis()
    //java反射绑定-冷启动5ms左右
    //注意:这里的耗时主要是花在了初始化lazy属性上,和反射本身没有太大关系
    javaClass.declaredFields
        .filter { it.isPresenterField() }
        .forEach {
            val presenter = it.getFieldValue(this) as? BaseMvpPresenter<*>
                ?: throw NullPointerException("$this $it must not be null!")
            presenter.attach(this)
        }
    //kotlin反射绑定-冷启动1800ms左右,kotlin的反射慎用
//    javaClass.kotlin.declaredMemberProperties.forEach {
//        it.isAccessible = true
//        (it.get(this) as? BaseMvpPresenter<*>)?.attach(this)
//    }
    Log.e("tag", "attachPresenter useTime: ${System.currentTimeMillis() - t1}")
}

/**
 * 反射获取字段对应的值
 */
private fun Field.getFieldValue(obj: Any): Any? {
    isAccessible = true
    val value = get(obj)
    return if (value is Lazy<*>) {
        value.value
    } else {
        value
    }
}

/**
 * 判断是否是presenter类型的字段
 */
private fun Field.isPresenterField(): Boolean {
    return if (type == Lazy::class.java) {
        //Lazy类型的字段(延迟初始化类型)
        isLazyPresenterField(this)
    } else {
        //常规类型字段
        isPresenterType(type)
    }
}

/**
 * 判断是否是Lazy类型的presenter字段
 */
private fun isLazyPresenterField(field: Field): Boolean {
    val method = findLazyFieldGetterMethod(field) ?: return false
    return isPresenterType(method.returnType)
}

/**
 * 获取Lazy类型字段对应的getter方法
 * 每一个Lazy类型的字段都对应一个getter方法,通过getter方法可以获取这个字段的真实类型
 */
private fun findLazyFieldGetterMethod(field: Field): Method? {
    if (field.type != Lazy::class.java) return null
    try {
        val fieldName = field.name
        val index = fieldName.indexOf("$")
        if (index == -1) return null
        val methodName = "get" + toUpperFirstCode(fieldName.substring(0, index))
        return field.declaringClass.getDeclaredMethod(methodName)
    } catch (t: Throwable) {
        Log.e("tag", "getLazyFieldGetterMethod error: $t")
    }
    return null
}

/**
 * 判断class是否是presenter类型
 */
private fun isPresenterType(clazz: Class<*>): Boolean {
    val superclass = clazz.superclass ?: return false
    return if (superclass == BaseMvpPresenter::class.java) {
        true
    } else {
        isPresenterType(superclass)
    }
}

/**
 * 将给定的字符串首字母转大写
 */
private fun toUpperFirstCode(s: String): String {
    if (s.isEmpty()) return ""
    val charArray = s.toCharArray()
    charArray[0] = charArray[0] - 32
    return String(charArray)
}