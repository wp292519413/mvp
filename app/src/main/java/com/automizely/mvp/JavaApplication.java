package com.automizely.mvp;

import android.app.Application;
import android.util.Log;

import com.automizely.login.di.LoginModuleKt;
import com.automizely.mvp.di.AppModuleKt;

import org.koin.android.java.KoinAndroidApplication;
import org.koin.core.Koin;
import org.koin.core.KoinApplication;
import org.koin.core.context.ContextFunctionsKt;
import org.koin.core.context.GlobalContext;
import org.koin.core.logger.Level;
import org.koin.core.module.Module;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.plugins.RxJavaPlugins;

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2021/1/6
 */
public class JavaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initKoin();
        setRxJavaErrorHandler();
    }

    private void initKoin() {
        List<Module> modules = new ArrayList<>();
        modules.addAll(AppModuleKt.getAppModule());
        modules.add(LoginModuleKt.getLoginModule());
        KoinApplication koinApplication = KoinAndroidApplication.create(this, Level.INFO);
        Koin koin = koinApplication.getKoin();
        koin.loadModules(modules);
        koin.createRootScope();
        ContextFunctionsKt.startKoin(new GlobalContext(), koinApplication);
    }

    private void setRxJavaErrorHandler() {
        RxJavaPlugins.setErrorHandler(t -> Log.d("tag", "RxJavaPlugins ErrorHandler: " + t));
    }

}
