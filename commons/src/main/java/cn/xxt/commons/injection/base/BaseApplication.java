package cn.xxt.commons.injection.base;

import android.app.Application;

/**
 * Created by Luke on 16/4/26.
 */
public class BaseApplication extends Application {

    private static Application application;

    public static Application getInstance() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }



}
