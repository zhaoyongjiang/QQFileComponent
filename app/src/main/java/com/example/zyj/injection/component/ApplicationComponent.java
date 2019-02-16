package com.example.zyj.injection.component;

import android.app.Application;
import android.content.Context;

import com.example.zyj.injection.module.ApplicationModule;

import javax.inject.Singleton;

import cn.xxt.commons.injection.ApplicationContext;
import dagger.Component;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    @ApplicationContext Context context();
    Application application();
}
