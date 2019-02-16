package cn.xxt.file.ui.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by zyj on 2017/8/16.
 */

public class FileSelectorMainViewPager extends ViewPager {
    private boolean isPagingEnabled = false;

    public FileSelectorMainViewPager(Context context) {
        super(context);
    }

    public FileSelectorMainViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.isPagingEnabled && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return this.isPagingEnabled && super.onInterceptTouchEvent(event);
    }

    public void setIsPagingEnabled(boolean active) {
        isPagingEnabled = active;
    }
}
