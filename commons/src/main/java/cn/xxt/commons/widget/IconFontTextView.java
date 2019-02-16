package cn.xxt.commons.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

/**
 * Created by Luke on 16/5/11.
 */
public class IconFontTextView extends TextView {

    private Context mContext;

    public IconFontTextView(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public IconFontTextView(Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    private void initView()
    {
        Typeface iconfont = Typeface.createFromAsset(mContext.getAssets(), "iconfont.ttf");
        setTypeface(iconfont);
    }
}
