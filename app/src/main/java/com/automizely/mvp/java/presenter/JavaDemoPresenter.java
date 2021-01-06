package com.automizely.mvp.java.presenter;

import com.automizely.framework.rx.AbsSingleObserver;
import com.automizely.mvp.java.contract.JavaDemoContract;
import com.automizely.mvp.user.model.User;
import com.automizely.mvp.user.model.UserModel;

import org.koin.java.KoinJavaComponent;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2021/1/6
 */
public class JavaDemoPresenter extends JavaDemoContract.AbsJavaDemoPresenter {

    private final UserModel mUserModel = KoinJavaComponent.get(UserModel.class);

    @Override
    public void loadUser() {
        mUserModel.login("zhangsan", "123")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new AbsSingleObserver<User>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onSuccess(@NonNull User user) {
                        getView().onLoadUserSuccess(user);
                    }

                    @Override
                    public void onError(@NonNull Throwable t) {
                        getView().onLoadUserFail(t);
                    }
                });
    }
}
