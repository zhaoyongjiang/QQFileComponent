package cn.xxt.commons.widget;

import android.content.Context;

import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.concurrent.TimeUnit;

import cn.xxt.commons.util.StringUtil;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Luke on 16/5/5.
 */
public class XXTHud {
    private static KProgressHUD hud;

    public static void show(Context context) {
        try {
            getInitedHud(context).show();
        } catch (Exception e) { //捕获窗口不存在的异常
            e.printStackTrace();
        }
    }

    public static void show(Context context, String tip) {
        try {
            getInitedHud(context).setLabel(tip).show();
        } catch (Exception e) { //捕获窗口不存在的异常
            e.printStackTrace();
        }
    }

    public static void show(Context context, String tip, boolean isCancellable) {
        try {
            getInitedHud(context).setLabel(tip).setCancellable(isCancellable).show();
        } catch (Exception e) { //捕获窗口不存在的异常
            e.printStackTrace();
        }
    }

    public static void show(Context context, String tip, String detailTip) {
        try {
            if (StringUtil.isEmpty(tip)) {
                tip = null;
            }
            if (StringUtil.isEmpty(detailTip)) {
                detailTip = null;
            }
            getInitedHud(context).setLabel(tip).setDetailsLabel(detailTip).show();
        } catch (Exception e) { //捕获窗口不存在的异常
            e.printStackTrace();
        }
    }

    public static void show(Context context, String tip, String detailTip, boolean isCancellable) {
        try {
            if (StringUtil.isEmpty(tip)) {
                tip = null;
            }
            if (StringUtil.isEmpty(detailTip)) {
                detailTip = null;
            }
            getInitedHud(context).setLabel(tip).setDetailsLabel(detailTip)
                    .setCancellable(isCancellable).show();
        } catch (Exception e) { //捕获窗口不存在的异常
            e.printStackTrace();
        }
    }

    public static void show(Context context,
                            String tip, String detailTip,
                            boolean isCancellable, KProgressHUD.Style style) {
        try {
            if (StringUtil.isEmpty(tip)) {
                tip = null;
            }
            if (StringUtil.isEmpty(detailTip)) {
                detailTip = null;
            }

            getInitedHud(context).setStyle(style).setLabel(tip).setDetailsLabel(detailTip)
                    .setCancellable(isCancellable).show();
        } catch (Exception e) { //捕获窗口不存在的异常
            e.printStackTrace();
        }
    }

    public static void dismiss() {
        try {
            if (hud!=null) {
                hud.dismiss();
            }
        } catch (Exception e) { //捕获窗口不存在的异常

        }
    }

    /**
     * 延迟消失
     * @param seconds
     */
    public static void dismissWithDelay(int seconds) {
        try {
            //Observable不能直接.delay()
            Observable.just(seconds)
                    .delay(seconds, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Integer>() {
                        @Override
                        public void call(Integer seconds) {
                            try {
                                hud.dismiss();
                            } catch (Exception e) {

                            }
                        }
                    });
        } catch (Exception e) {

        }
    }

    private static KProgressHUD getInitedHud(Context context) {
        if (hud!=null) {
            hud.dismiss();
            hud = null;
        }

        if (hud==null) {
            hud = KProgressHUD.create(context);
        }
        hud.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel(null);

        return hud;
    }

}
