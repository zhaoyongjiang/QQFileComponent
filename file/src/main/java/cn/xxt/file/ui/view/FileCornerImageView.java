package cn.xxt.file.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

/**
 * Created by zyj on 2018/12/13.
 */
public class FileCornerImageView extends androidx.appcompat.widget.AppCompatImageView {
    //圆角弧度
    private float[] rids = {6.0f,6.0f,6.0f,6.0f,6.0f,6.0f,6.0f,6.0f,};

    public FileCornerImageView(Context context) {
        super(context);
    }

    public FileCornerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FileCornerImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Path path = new Path();
        int w = this.getWidth();
        int h = this.getHeight();
        //绘制圆角imageview
        path.addRoundRect(new RectF(0,0,w,h),rids,Path.Direction.CW);
        canvas.clipPath(path);
        super.onDraw(canvas);
    }
}
