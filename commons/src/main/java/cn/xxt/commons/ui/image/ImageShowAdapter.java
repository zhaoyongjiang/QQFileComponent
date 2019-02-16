package cn.xxt.commons.ui.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import cn.xxt.commons.R;
import cn.xxt.commons.util.StringUtil;

/**
 * Created by Luke on 16/6/17.
 */
public class ImageShowAdapter extends PagerAdapter {

    private List<String> urlList;
    private Context context;

    public ImageShowAdapter(Context context, List<String> urlList) {
        this.urlList = urlList;
        this.context = context;
    }

    @Override
    public int getCount() {
        int count = 0;
        if (null!=urlList) {
            count = urlList.size();
        }
        return count;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        PhotoView pvImage = (PhotoView) inflater.inflate(R.layout.item_image_show, null);

        if (null!=urlList && position>=0 && position<urlList.size()) {
            String url = urlList.get(position);

            //本地图片
            if (url != null
                    && url.length() > 0
                    && url.startsWith("/") && !url.startsWith("//")) {
                // "//"双斜杠，是协议自适应的，所以，这里需要走到网络加载.
                    Picasso.get().load(StringUtil.connectStrings("file://",url))
                            .placeholder(R.drawable.bg_placeholder_loading)
                            .error(R.drawable.bg_placeholder_load_fail)
                            .resizeDimen(R.dimen.max_image_size,R.dimen.max_image_size)
                            .onlyScaleDown()
                            .centerInside()
                            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                            .config(Bitmap.Config.RGB_565)
                            .into(pvImage);
            } else {
                // https 转 http 针对没有协议的url，这不处理会添加上http协议
                url = StringUtil.urlStr2HttpUrlStr(url);
                Picasso.get().load(url)
                        .placeholder(R.drawable.bg_placeholder_loading)
                        .error(R.drawable.bg_placeholder_load_fail)
                        .resizeDimen(R.dimen.max_image_size,R.dimen.max_image_size)
                        .onlyScaleDown()
                        .centerInside()
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                        .config(Bitmap.Config.RGB_565)
                        .into(pvImage);
            }
        }

        container.addView(pvImage);
        return pvImage;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }
}
