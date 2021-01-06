package com.automizely.mvp.java;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.automizely.framework.mvp.BaseMvpActivity;
import com.automizely.framework.mvp.MvpExt;
import com.automizely.mvp.R;
import com.automizely.mvp.java.contract.JavaDemoContract;
import com.automizely.mvp.java.presenter.JavaDemoPresenter;
import com.automizely.mvp.user.model.User;

/**
 * @author: wangpan
 * @emial: p.wang@aftership.com
 * @date: 2021/1/6
 */
public class JavaDemoActivity extends BaseMvpActivity implements JavaDemoContract.IJavaDemoView {

    private final JavaDemoPresenter mPresenter = MvpExt.getPresenter(JavaDemoPresenter.class, this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_java_demo);
        findViewById(R.id.btn_test).setOnClickListener(v -> mPresenter.loadUser());
    }

    @Override
    public void onLoadUserSuccess(@NonNull User user) {
        Toast.makeText(this, "onLoadUserSuccess", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoadUserFail(@NonNull Throwable t) {
        Toast.makeText(this, "onLoadUserFail", Toast.LENGTH_SHORT).show();
    }
}
