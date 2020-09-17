package com.automizely.framework.utils

import org.greenrobot.eventbus.EventBus

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020-02-03
 */
object EventBusUtil {

    /**
     * 注册
     */
    fun register(obj: Any) {
        if (!EventBus.getDefault().isRegistered(obj)) {
            EventBus.getDefault().register(obj)
        }
    }

    /**
     * 反注册
     */
    fun unregister(obj: Any) {
        if (EventBus.getDefault().isRegistered(obj)) {
            EventBus.getDefault().unregister(obj)
        }
    }

    /**
     * 发送普通事件
     */
    fun post(event: Any) {
        EventBus.getDefault().post(event)
    }

    /**
     * 发送粘性事件
     */
    fun postSticky(event: Any) {
        EventBus.getDefault().postSticky(event)
    }

    /**
     * 移除事件
     */
    fun removeStickyEvent(event: Any) {
        EventBus.getDefault().removeStickyEvent(event)
    }
}