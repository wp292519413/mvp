package com.automizely.mvp.java.di;

import com.automizely.framework.mvp.ModuleJava;
import com.automizely.mvp.java.presenter.JavaDemoPresenter;

import org.koin.core.module.Module;

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2021/1/6
 */
public class JavaDemoModule {

    public static final Module javaDemoModule = new Module(false, false);

    static {
        ModuleJava.factory(javaDemoModule, JavaDemoPresenter.class, (scope, definitionParameters) -> new JavaDemoPresenter());
    }

}
