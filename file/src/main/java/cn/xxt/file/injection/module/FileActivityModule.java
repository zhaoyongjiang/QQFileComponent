package cn.xxt.file.injection.module;

import android.app.Activity;
import android.content.Context;

import cn.xxt.commons.injection.ActivityContext;
import dagger.Module;
import dagger.Provides;

/**
 * Created by zyj on 2017/8/16.
 */

@Module
public class FileActivityModule {
    private Activity activity;

    public FileActivityModule(Activity activity) {
        this.activity = activity;
    }

    @Provides
    Activity provideActivity() {
        return activity;
    }

    @Provides
    @ActivityContext
    Context provideContext() {
        return activity;
    }
}
