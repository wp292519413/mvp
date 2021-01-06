package com.automizely.mvp.java.contract;

import androidx.annotation.NonNull;

import com.automizely.framework.mvp.BaseMvpPresenter;
import com.automizely.framework.mvp.BaseMvpView;
import com.automizely.mvp.user.model.User;

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2021/1/6
 */
public interface JavaDemoContract {

    interface IJavaDemoView extends BaseMvpView {

        void onLoadUserSuccess(@NonNull User user);

        void onLoadUserFail(@NonNull Throwable t);
    }

    abstract class AbsJavaDemoPresenter extends BaseMvpPresenter<IJavaDemoView> {

        public abstract void loadUser();
    }

}
