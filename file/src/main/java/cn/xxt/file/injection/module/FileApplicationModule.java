package cn.xxt.file.injection.module;

import android.app.Application;
import android.content.Context;

import cn.xxt.commons.injection.ApplicationContext;
import dagger.Module;
import dagger.Provides;

/**
 * Created by zyj on 2017/8/16.
 */

@Module
public class FileApplicationModule {
    protected final Application application;

    public FileApplicationModule(Application application) {
        this.application = application;
    }

    @Provides
    Application provideApplication() {
        return application;
    }

    @Provides
    @ApplicationContext
    Context provideContext() {
        return application;
    }

}
