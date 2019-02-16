package com.example.zyj.ui.base;

import android.os.Bundle;

import com.example.zyj.MyApplication;
import com.example.zyj.injection.component.ActivityComponent;
import com.example.zyj.injection.component.DaggerActivityComponent;
import com.example.zyj.injection.module.ActivityModule;

import cn.xxt.library.ui.base.XXTSupportActivity;

public class AppBaseActivity extends XXTSupportActivity {

    private ActivityComponent activityComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public ActivityComponent getActivityComponent() {
        if (activityComponent == null) {
            activityComponent = DaggerActivityComponent.builder()
                    .activityModule(new ActivityModule(this))
                    .applicationComponent(MyApplication.get(this).getComponent())
                    .build();
        }
        return activityComponent;
    }
}
