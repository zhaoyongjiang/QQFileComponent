package com.example.zyj.injection.component;

import com.example.zyj.injection.module.ActivityModule;
import com.example.zyj.ui.MainActivity;

import cn.xxt.commons.injection.PerActivity;
import dagger.Component;

/**
 * This component inject dependencies to all Activities across the application
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {

    void inject(MainActivity mainActivity);
}

