package com.example.zyj;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.example.zyj.injection.component.ApplicationComponent;
import com.example.zyj.injection.component.DaggerApplicationComponent;
import com.example.zyj.injection.module.ApplicationModule;
import com.tencent.smtt.sdk.QbSdk;

import androidx.multidex.MultiDex;
import cn.xxt.commons.injection.base.BaseApplication;
import cn.xxt.commons.util.SwitchOfHttps;
import cn.xxt.file.api.FileComponent;
import me.yokeyword.fragmentation.Fragmentation;
import me.yokeyword.fragmentation.helper.ExceptionHandler;

public class MyApplication extends BaseApplication {
    private ApplicationComponent applicationComponent;
    private int activityCount;

    @Override
    public void onCreate() {
        super.onCreate();

        SwitchOfHttps.setCloseHttps(false);

        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
            @Override
            public void onViewInitFinished(boolean arg0) {
                // TODO Auto-generated method stub
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                Log.d("app", " onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
                // TODO Auto-generated method stub
            }
        };

        //X5内核初始化
        QbSdk.initX5Environment(getApplicationContext(), cb);

        FileComponent.builder(getApplicationContext())
                .setMaxNum(9)
                .setMaxSize(10, FileComponent.SizeUnitEnum.MB_TYPE)
                .setConcorrentDownloadMaxNum(3).setShowFileType(1,2,3,4,5,6);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                if (activityCount==0) {
                    willEnterForeground();
                }
                activityCount++;
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                activityCount--;
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

        Fragmentation.builder()
                // 设置 栈视图 模式为 （默认）悬浮球模式   SHAKE: 摇一摇唤出  NONE：隐藏， 仅在Debug环境生效
                .stackViewMode(Fragmentation.BUBBLE)
                .debug(BuildConfig.DEBUG) // 实际场景建议.debug(BuildConfig.DEBUG)
                /**
                 * 可以获取到{@link me.yokeyword.fragmentation.exception.AfterSaveStateTransactionWarning}
                 * 在遇到After onSaveInstanceState时，不会抛出异常，会回调到下面的ExceptionHandler
                 */
                .handleException(new ExceptionHandler() {
                    @Override
                    public void onException(Exception e) {
                        // 以Bugtags为例子: 把捕获到的 Exception 传到 Bugtags 后台。
                        // Bugtags.sendException(e);
                    }
                })
                .install();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static MyApplication get(Context context) {
        return (MyApplication) context.getApplicationContext();
    }

    public ApplicationComponent getComponent() {
        if (applicationComponent == null) {
            applicationComponent = DaggerApplicationComponent.builder()
                    .applicationModule(new ApplicationModule(this))
                    .build();
        }
        return applicationComponent;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        System.exit(0);
    }

    /**
     * Needed to replace the component with a test specific one
     * @param applicationComponent
     */
    public void setComponent(ApplicationComponent applicationComponent) {
        this.applicationComponent = applicationComponent;
    }

    public boolean isForeground() {
        return activityCount > 0;
    }

    public void willEnterForeground() {
//        Login.setNeedRefreshLoginFlag(getApplicationContext(),true);
    }
}
