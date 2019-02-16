package cn.xxt.commons.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Luke on 17/2/13.
 */

public class RecyclerImageView extends ImageView {

    public RecyclerImageView(Context context) {
        super(context);
    }

    public RecyclerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    //本意想view被销毁时，清空drawable，这样能快速回收内存，但是在滚动上，也会触发这个问题，导致图片空白
    //因此弃用
//    @Override
//    protected void onDetachedFromWindow() {
//        super.onDetachedFromWindow();
//        setImageDrawable(null);
//    }

}
