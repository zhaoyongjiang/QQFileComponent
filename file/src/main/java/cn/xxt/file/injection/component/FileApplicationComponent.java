package cn.xxt.file.injection.component;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import cn.xxt.commons.injection.ApplicationContext;
import cn.xxt.file.injection.module.FileApplicationModule;
import dagger.Component;

/**
 * Created by zyj on 2017/8/16.
 */

@Singleton
@Component(modules = FileApplicationModule.class)
public interface FileApplicationComponent {
    //Component嵌套，为上层Component提供依赖
    @ApplicationContext
    Context context();
    Application application();
}
