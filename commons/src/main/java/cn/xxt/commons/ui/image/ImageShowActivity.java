package cn.xxt.commons.ui.image;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.xxt.commons.R;
import cn.xxt.commons.ui.base.TranslucentActivity;
import cn.xxt.commons.util.ImageUtil;
import cn.xxt.commons.util.StringUtil;
import cn.xxt.commons.util.ToastUtil;
import cn.xxt.commons.widget.IconFontTextView;
import cn.xxt.commons.widget.XXTHud;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 图片查看器，支持查看网络及本地图片，本地图片以/开头，
 *
 * 使用说明：
 *
 * 查看图片：
 * 将图片(本地路径或网络图片链接)的List，在Bundle中以key为BUNDLE_IMAGE_LIST传入，
 * 如需要默认显示第几张，则以key为BUNDLE_SELECTED_INDEX传入。
 * 示例：
 * ArrayList<String> urlList = new ArrayList<>();
 * urlList.add("http://www.xxx.xx/1.jpg");
 * urlList.add("/storage/sdcard0/1.jpg");
 * Bundle bundle =  new Bundle();
 * bundle.putStringArrayList(ImageShowActivity.BUNDLE_IMAGE_LIST,urlList);
 * bundle.putInt(ImageShowActivity.BUNDLE_SELECTED_INDEX,2);
 * ActivityUtil.changeActivity(LoginActivity.this,ImageShowActivity.class,bundle,false);
 *
 * 查看并选择图片：
 * 在查看图片的基础上，在Bundle中以key传入BUNDLE_SELECTED_LIST，
 * 再自己继承onActivityResult接收选择图片列表，key为BUNDLE_SELECTED_LIST，值为ArrayList<String>
 * 若bundle中有key为BUNDLE_RESULT_IS_CONFIRM且值为true，则代表是点击确定键
 * 示例见：ImageChooseActivity
 *
 *
 *
 * Created by Luke on 16/6/17.
 */
public class ImageShowActivity extends TranslucentActivity implements ViewPager.OnPageChangeListener {

    /** 图片list的key，传值为arraylist */
    public final static String BUNDLE_IMAGE_LIST = "BUNDLE_IMAGE_LIST";

    /** 默认展示第几个图片，传值为int */
    public final static String BUNDLE_SELECTED_INDEX = "BUNDLE_SELECTED_INDEX";

    /** 已选择图片列表，传值为arraylist，传这值默认进入查看&选择模式 */
    public final static String BUNDLE_SELECTED_LIST = "BUNDLE_SELECTED_LIST";

    /** 是否在选择模式下点右上角确定的，值为boolean */
    public final static String BUNDLE_RESULT_IS_CONFIRM = "BUNDLE_RESULT_IS_CONFIRM";

    /** 上传图片。选择本地图片后-预览，右下角不用提供保存功能 */
    public final static String BUNDLE_SHOW_SAVE_FUNC = "BUNDLE_SHOW_SAVE_FUNC";

    /** viewpager */
    private PhotoViewPager pvpImageShow;

    /** viewpager适配器 */
    private ImageShowAdapter imageShowAdapter;

    /** 图片list */
    private List<String> imageList = new ArrayList();

    /** 已选择图片list */
    private ArrayList<String> selectedImageList = new ArrayList<>();

    private int currentIndex;

    private IconFontTextView iftvTopLeft;

    private IconFontTextView iftvTopRight;

    private FloatingActionButton saveImgFab;

    /** 是否带选择功能 */
    private boolean isSelectMode;

    private TextView tvTopTitle;

    /** 已选择数量textview */
    private TextView tvSelectedNum;

    /** 选择复选框 */
    private IconFontTextView iftvCheck;

    /** 选择图片的最大张数，默认为1*/
    private int selectPictureMaxNum = 1;

    private boolean showSaveFunc = false;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_show);

        context = this;

        Bundle bundle = this.getIntent().getExtras();
        if (bundle.containsKey(BUNDLE_IMAGE_LIST)) {
            imageList.addAll((ArrayList<String>) getIntent().getSerializableExtra(BUNDLE_IMAGE_LIST));
        }

        if (bundle.containsKey(BUNDLE_SELECTED_INDEX)) {
            currentIndex = bundle.getInt(BUNDLE_SELECTED_INDEX);
        }

        if (bundle.containsKey(BUNDLE_SELECTED_LIST)) {
            isSelectMode = true;
            selectedImageList.addAll((ArrayList<String>) getIntent()
                    .getSerializableExtra(BUNDLE_SELECTED_LIST));
        } else {
            isSelectMode = false;
        }

        if (bundle.containsKey(ImageChooseActivity.BUNDLE_SELECT_PICTURE_MAX_NUM)) {
            selectPictureMaxNum = bundle.getInt(ImageChooseActivity
                    .BUNDLE_SELECT_PICTURE_MAX_NUM, 1);
            if (selectPictureMaxNum <= 0) {
                selectPictureMaxNum = 1;
            }
        }

        if (bundle.containsKey(BUNDLE_SHOW_SAVE_FUNC)) {
            showSaveFunc = bundle.getBoolean(BUNDLE_SHOW_SAVE_FUNC, false);
        }

        initViews();
    }



    @Override
    public void onPageSelected(int position) {
        currentIndex = position;
        updateTitle();
        updateBottom();
        imageShowAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * 更新标题栏
     */
    private void updateTitle() {
        if (imageList !=null && currentIndex>=0 && currentIndex< imageList.size()) {
            tvTopTitle.setText(String.valueOf(currentIndex + 1) + "/" + String.valueOf(imageList.size()));
        }
    }

    /**
     * 更新底部展示栏
     */
    private void updateBottom() {
        if(isSelectMode) {
            updateCheckStatus();
            updateSelectedNum();
        }
    }

    /**
     * 更新已选择张数
     */
    private void updateSelectedNum() {
        int num = 0;
        if (selectedImageList!=null) {
            num = selectedImageList.size();
        }

        tvSelectedNum.setText("已选择 " + String.valueOf(num) + " 张");
    }

    /**
     * 更新选中状态
     */
    private void updateCheckStatus() {
        boolean flag = false;
        if (selectedImageList!=null
                && imageList!=null && currentIndex>=0 && currentIndex<imageList.size()) {
            flag = selectedImageList.contains(imageList.get(currentIndex));
        }
        if (flag) {
            iftvCheck.setText(getString(R.string.iconfont_choosed_fill));
        } else {
            iftvCheck.setText(getString(R.string.iconfont_not_choosed));
        }
    }

    /**
     * 初始化界面
     */
    private void initViews() {
        iftvTopLeft = (IconFontTextView)findViewById(R.id.iftv_top_left);
        iftvTopRight = (IconFontTextView)findViewById(R.id.iftv_top_right);
        tvTopTitle = (TextView)findViewById(R.id.tv_top_title);

        iftvTopLeft.setText(getString(R.string.iconfont_back));
        iftvTopLeft.setVisibility(View.VISIBLE);

        tvTopTitle.setText(getString(R.string.title_image_show));

        iftvTopRight.setText(getString(R.string.save));
        iftvTopRight.setTextSize(16);
        iftvTopRight.setVisibility(View.VISIBLE);

        pvpImageShow = (PhotoViewPager) findViewById(R.id.pvp_image_show);

        saveImgFab = (FloatingActionButton) findViewById(R.id.fab_save_image);
        if (showSaveFunc) {
            saveImgFab.setVisibility(View.VISIBLE);
        } else {
            saveImgFab.setVisibility(View.GONE);
        }

        //选择图片模式
        if (isSelectMode) {
            iftvTopRight.setText(getString(R.string.confirm));

            RelativeLayout rlBottom = (RelativeLayout)findViewById(R.id.rl_bottom);
            rlBottom.setVisibility(View.VISIBLE);

            tvSelectedNum = (TextView)findViewById(R.id.tv_selected_num);
            iftvCheck = (IconFontTextView)findViewById(R.id.iftv_check);
            updateBottom();

            RelativeLayout rlCheck = (RelativeLayout)findViewById(R.id.rl_check);

            //底部栏右侧选择控件点击事件
            RxView.clicks(rlCheck)
                    .compose(this.<Void>bindToLifecycle())
                    .throttleFirst(300, TimeUnit.MILLISECONDS)
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
                            if (selectedImageList !=null && imageList != null
                                    && currentIndex >= 0 && currentIndex < imageList.size()) {
                                String url = imageList.get(currentIndex);
                                if (selectedImageList.contains(url)) {
                                    selectedImageList.remove(url);
                                } else {
                                    if (selectedImageList.size() >= selectPictureMaxNum) {
                                        ToastUtil.displayToastShort(context,
                                                StringUtil.connectStrings("选择的图片已经达到上限",
                                                        String.valueOf(selectPictureMaxNum), "张"));
                                    } else {
                                        selectedImageList.add(url);
                                    }
                                }
                                updateBottom();
                            }
                        }
                    });
        }

        pvpImageShow.addOnPageChangeListener(this);

        imageShowAdapter = new ImageShowAdapter(context, imageList);
        pvpImageShow.setAdapter(imageShowAdapter);

        if (currentIndex< imageList.size()) {
            pvpImageShow.setCurrentItem(currentIndex);
            updateTitle();
        }

        //返回按钮点击事件
        RxView.clicks(iftvTopLeft)
                .compose(this.<Void>bindToLifecycle())
                .throttleFirst(2, TimeUnit.SECONDS)
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
                        if (isSelectMode) {
                            prepareResult(false);
                        }
                        ImageShowActivity.this.finish();
                    }
                });

        //右上角按钮点击事件
        RxView.clicks(iftvTopRight)
                .compose(this.<Void>bindToLifecycle())
                .throttleFirst(2, TimeUnit.SECONDS)
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
                        if (isSelectMode) {
                            prepareResult(true);
                            ImageShowActivity.this.finish();
                        } else {
                            saveImage();
                        }
                    }
                });

        RxView.clicks(saveImgFab)
                .throttleFirst(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Void>bindToLifecycle())
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

                        saveImage();
                    }
                });
    }

    /**
     * 准备返回结果
     * @param isConfirm 是否是点击右上角确定按钮
     */
    private void prepareResult(boolean isConfirm) {
        Intent intent = new Intent();

        Bundle bundle = new Bundle();
        bundle.putStringArrayList(BUNDLE_SELECTED_LIST,selectedImageList);
        if (isConfirm) {
            bundle.putBoolean(BUNDLE_RESULT_IS_CONFIRM,true);
        }
        intent.putExtras(bundle);

        setResult(RESULT_OK,intent);
    }

    /**
     * 保存图片
     */
    private void saveImage() {
        XXTHud.show(context,getString(R.string.saving_image));
        Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                boolean flag = false;
                if (imageList !=null && currentIndex>=0 && currentIndex< imageList.size()) {
                    String url = imageList.get(currentIndex);

                    if (url.startsWith("/")) {
                        url = StringUtil.connectStrings("file://",url);
                    }

                    try {
                        Bitmap bitmap = Picasso.get().load(url)
                                .resizeDimen(R.dimen.max_image_size,R.dimen.max_image_size)
                                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                .config(Bitmap.Config.RGB_565)
                                .onlyScaleDown()
                                .centerInside()
                                .get();
                        flag = ImageUtil.saveImage(context,bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                subscriber.onNext(flag);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtil.displayToastLong(context,
                                getString(R.string.save_image_failed));
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Boolean flag) {
                        XXTHud.dismiss();
                        if (flag) {
                            ToastUtil.displayToastLong(context,"图片已保存至 "
                                    + ImageUtil.getXxtAlbumPath() + " 文件夹");
                        } else {
                            ToastUtil.displayToastLong(context,
                                    getString(R.string.save_image_failed));
                        }
                    }
                });
    }

    @Override
    protected String getActivityName() {
        return "图片查看页面";
    }

}
