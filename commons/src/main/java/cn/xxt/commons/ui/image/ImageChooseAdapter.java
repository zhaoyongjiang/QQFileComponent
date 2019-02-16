package cn.xxt.commons.ui.image;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jakewharton.rxbinding.view.RxView;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.xxt.commons.R;
import cn.xxt.commons.util.StringUtil;
import cn.xxt.commons.widget.IconFontTextView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * 选择图片适配器
 * Created by Luke on 16/6/21.
 */
public class ImageChooseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public interface OnImageChooseListener {
        /** 点击右上角选择区域点击事件 */
        void onImageChooseTagClicked(int position);

        /** 点击图片本身点击事件 */
        void onImageChooseImageClicked(int position);

        void onImageTakeClicked();
    }

    private static final int ITEM_TYPE_IMAGE = 0;

    private static final int ITEM_TYPE_TAKE_PHOTO = 1;

    private Context context;

    /** 图片列表 */
    private List<String> imageList;

    /** 已选择图片列表 */
    private List<String> selectedImageList;

    /** 是否显示复选框 */
    private boolean photoOptional = true;
    public void setPhotoOptional(boolean photoOptional) {
        this.photoOptional = photoOptional;
    }

    /** 点击事件监听器 */
    private OnImageChooseListener listener;

    public ImageChooseAdapter(Context context, List<String> imageList, List<String> selectedImageList){
        this.context = context;
        this.imageList = imageList;
        this.selectedImageList = selectedImageList;
    }

    /**
     * 创建viewholder
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        RecyclerView rvImages = (RecyclerView)parent;
        GridLayoutManager layoutManager = (GridLayoutManager) rvImages.getLayoutManager();
        int spanCount = layoutManager.getSpanCount();
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(rvImages.getWidth()/spanCount,
                rvImages.getWidth()/spanCount);

        if(viewType == ITEM_TYPE_TAKE_PHOTO) {
            view = LayoutInflater.from(context).inflate(R.layout.item_image_take, parent, false);
            view.setLayoutParams(lp);

            return new ImageTakeViewHolder(view);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_image_choose, parent, false);
            view.setLayoutParams(lp);

            return new ViewHolder(view);
        }
    }

    /**
     * viewholder配置
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ImageTakeViewHolder) {
            RxView.clicks(((ImageTakeViewHolder)holder).itemView)
                    .throttleFirst(1, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Void aVoid) {
                            if (listener != null) {
                                listener.onImageTakeClicked();
                            }
                        }
                    });
        } else if (holder instanceof ViewHolder) {
            final int pos = position - 1;

            String url = "";
            if (imageList != null && pos >= 0 && pos < imageList.size()) {
                url = imageList.get(pos);
            }

            Picasso.get().load(StringUtil.connectStrings("file://",url))
                    .placeholder(R.drawable.bg_placeholder_loading)
                    .error(R.drawable.bg_placeholder_load_fail)
                    .resize(200, 200)
                    .centerCrop()
                    .into(((ViewHolder)holder).ivImage);

            //选中
            if (photoOptional) {
                ((ViewHolder)holder).iftvChooseTag.setVisibility(View.VISIBLE);
                if (selectedImageList != null && selectedImageList.contains(url)) {
                    ((ViewHolder)holder).iftvChooseTag.setText(
                            context.getString(R.string.iconfont_choosed_fill));
                    ((ViewHolder)holder).vCover.setVisibility(View.VISIBLE);
                } else { //未选中
                    ((ViewHolder)holder).iftvChooseTag.setText(
                            context.getString(R.string.iconfont_not_choosed));
                    ((ViewHolder)holder).vCover.setVisibility(View.GONE);
                }
            } else {
                ((ViewHolder)holder).iftvChooseTag.setVisibility(View.GONE);
            }

            RxView.clicks(((ViewHolder)holder).ivImage)
                    .throttleFirst(1, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Void aVoid) {
                            if (listener != null) {
                                listener.onImageChooseImageClicked(pos);
                            }
                        }
                    });

            RxView.clicks(((ViewHolder)holder).iftvChooseTag)
                    .throttleFirst(1, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Void aVoid) {
                            if (listener != null) {
                                listener.onImageChooseTagClicked(pos);
                            }
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        int count = 1;
        if (imageList!=null) {
            count += imageList.size();
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (position==0) {
            return ITEM_TYPE_TAKE_PHOTO;
        } else {
            return ITEM_TYPE_IMAGE;
        }
    }

    public void setOnImageChooseListener(OnImageChooseListener listener) {
        this.listener = listener;
    }

    /**
     * 自定义的ViewHolder，持有每个Item的的所有界面元素
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivImage;
        public IconFontTextView iftvChooseTag;
        public View vCover;
        public ViewHolder(View view) {
            super(view);
            ivImage = (ImageView) view.findViewById(R.id.iv_image);
            iftvChooseTag = (IconFontTextView) view.findViewById(R.id.iftv_choose_tag);
            vCover = view.findViewById(R.id.v_cover);
        }
    }

    public static class ImageTakeViewHolder extends RecyclerView.ViewHolder {

        public ImageTakeViewHolder(View itemView) {
            super(itemView);
        }
    }

}
