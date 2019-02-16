package cn.xxt.commons.ui.image;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import cn.yangbingqiang.android.parallaxviewpager.ParallaxViewPager;

/**
 * Created by Luke on 17/1/23.
 * 解决  photoview 与viewpager 组合时 图片缩放的错误 ；
 * 异常：.IllegalArgumentException: pointerIndex out of range
 */

public class PhotoViewPager extends ParallaxViewPager {

    public PhotoViewPager(Context context) {
        super(context);
    }

    public PhotoViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (Throwable e) {
            // ignore it
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (Throwable e) {
            // ignore it
        }
        return false;
    }

}
