package cn.xxt.commons.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Luke on 16/5/4.
 */
public class ToastUtil {

    /**
     * 显示短提示
     *
     * @param context
     *            当前的context
     * @param str
     *            提示信息
     */
    public static void displayToastShort(final Context context, final String str) {
        Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        Toast toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                });
    }

    /**
     * 显示长提示
     *
     * @param context
     *            当前的context
     * @param str
     *            提示信息
     */
    public static void displayToastLong(final Context context, final String str) {
        Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        Toast toast = Toast.makeText(context, str, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                });
    }
}
