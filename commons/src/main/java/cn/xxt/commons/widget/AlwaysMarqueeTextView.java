package cn.xxt.commons.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 *自动实现文字滚动效果的TextView
 *@author yinzhuangzhuang
 *@version 1.0
 *20122012-6-29上午10:22:15
 */
public class AlwaysMarqueeTextView extends TextView {

	public AlwaysMarqueeTextView(Context context) {
		super(context);
	}

	public AlwaysMarqueeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AlwaysMarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean isFocused() {
		return true;
	}
}	
