package cn.xxt.commons.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by Luke on 16/5/4.
 */
public class ActivityUtil {


    /**
     * 切换Activity
     *
     * @param pre  发起跳转的Activity实例
     * @param next 要跳转到的Activity的class
     * @param bundle  附带参数,没有的话传null
     * @param closeFlag 是否关闭发起跳转的Activity
     */
    public static void changeActivity(Activity pre,
                                 Class<? extends Activity> next,
                                 Bundle bundle,
                                 boolean closeFlag) {
        // 新建一个Intent
        Intent intent = new Intent();
        if (bundle != null) {
            // 设置bundle
            intent.putExtras(bundle);
        }
        // 设置Intent要启动的类
        intent.setClass(pre, next);
        // 启动新的Activity
        pre.startActivity(intent);

        if (closeFlag) {
            // 关闭当前的Activity
            pre.finish();
        }
    }

    /**
     * 按类名称切换Activity
     *
     * @param pre  发起跳转的Activity实例
     * @param className 要跳转到的Activity的类全路径
     * @param bundle  附带参数,没有的话传null
     * @param closeFlag 是否关闭发起跳转的Activity
     */
    public static void changeActivity(Activity pre,
                                 String className,
                                 Bundle bundle,
                                 boolean closeFlag) {
        // 新建一个Intent
        Intent intent = new Intent();
        if (bundle != null) {
            // 设置bundle
            intent.putExtras(bundle);
        }
        // 设置Intent要启动的类
        intent.setClassName(pre, className);
        // 启动新的Activity
        pre.startActivity(intent);

        if (closeFlag) {
            // 关闭当前的Activity
            pre.finish();
        }
    }

    /**
     * 切换Activity
     *
     * @param pre  发起跳转的Activity实例
     * @param next 要跳转到的Activity的class
     * @param bundle  附带参数,没有的话传null
     * @param closeFlag 是否关闭发起跳转的Activity
     * @param intentFlags
     */
    public static void changeActivityAndSetFlags(Activity pre,
                                      Class<? extends Activity> next,
                                      Bundle bundle,
                                      boolean closeFlag,
                                      int intentFlags) {
        // 新建一个Intent
        Intent intent = new Intent();
        if (bundle != null) {
            // 设置bundle
            intent.putExtras(bundle);
        }
        // 设置Intent要启动的类
        intent.setClass(pre, next);

        //设置intentflag
        intent.addFlags(intentFlags);

        // 启动新的Activity
        pre.startActivity(intent);

        if (closeFlag) {
            // 关闭当前的Activity
            pre.finish();
        }

    }

    /**
     * 切换Activity
     *
     * @param pre  发起跳转的Activity实例
     * @param className 要跳转到的Activity的类全路径
     * @param bundle  附带参数,没有的话传null
     * @param closeFlag 是否关闭发起跳转的Activity
     * @param intentFlags
     */
    public static void changeActivityAndSetFlags(Activity pre,
                                                 String className,
                                                 Bundle bundle,
                                                 boolean closeFlag,
                                                 int intentFlags) {
        // 新建一个Intent
        Intent intent = new Intent();
        if (bundle != null) {
            // 设置bundle
            intent.putExtras(bundle);
        }
        // 设置Intent要启动的类
        intent.setClassName(pre, className);

        //设置intentflag
        intent.addFlags(intentFlags);

        // 启动新的Activity
        pre.startActivity(intent);

        if (closeFlag) {
            // 关闭当前的Activity
            pre.finish();
        }
    }

    /**
     * 生成关闭当前的activity的事件监听对象
     * @param activity
     */
    public static View.OnClickListener getOnClickCloseActivityListener(final Activity activity){
        return
                (new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.finish();
                    }
                });
    }

}
