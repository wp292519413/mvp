package com.automizely.framework

import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2020/9/16
 */
class AClass : KoinComponent {

    private val s: String by inject()

}