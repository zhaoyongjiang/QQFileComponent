package cn.xxt.file.ui.selector;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.xxt.commons.injection.ActivityContext;
import cn.xxt.commons.ui.image.ImageShowActivity;
import cn.xxt.commons.util.ActivityUtil;
import cn.xxt.commons.util.BundleUtil;
import cn.xxt.commons.util.RxBusWithTag;
import cn.xxt.commons.util.StringUtil;
import cn.xxt.commons.util.ToastUtil;
import cn.xxt.commons.widget.IconFontTextView;
import cn.xxt.file.R;
import cn.xxt.file.R2;
import cn.xxt.file.api.FileComponent;
import cn.xxt.file.internal.domain.FileInfo;
import cn.xxt.file.internal.domain.FileTypeEnum;
import cn.xxt.file.ui.base.FileBaseActivity;
import cn.xxt.file.ui.base.FileBaseFragment;
import cn.xxt.file.ui.base.FileTabPagerAdapter;
import cn.xxt.file.ui.fileFragment.FileLocalMainFragment;
import cn.xxt.file.ui.fileFragment.FileRecentMainFragment;
import cn.xxt.file.util.FileUtil;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static cn.xxt.file.api.FileComponentCommonValue.BUNDLE_SELECTED_FILE_LIST;
import static cn.xxt.file.api.FileComponentCommonValue.KEY_WEBID;

/**
 * Created by zyj on 2017/8/16.
 */

public class FileSelectorMainActivity extends FileBaseActivity {

    /** 顶部标题数组：本机，最近 */
    private List<String> fileSourceTypeList = new ArrayList<>();

    /** fragment数组：本机，最近 */
    private List<Fragment> fileSourceFragments = new ArrayList<>();

    @BindView(R2.id.file_selector_main_viewpager)
    ViewPager fileSelectorMainViewpager;

    @BindView(R2.id.main_top_rg)
    RadioGroup mainTopRg;
    
    @BindView(R2.id.top_rg_recent)
    RadioButton topRbRecent;

    @BindView(R2.id.top_rg_local)
    RadioButton topRbLocal;

    @BindView(R2.id.iftv_top_right)
    IconFontTextView iftvCancel;

    @BindView(R2.id.tv_preview)
    TextView tvPreview;

    @BindView(R2.id.tv_all_size)
    TextView tvSelectorSize;

    @BindView(R2.id.tv_send)
    TextView tvSend;

    @Inject
    @ActivityContext
    Context context;

    private Observable<Map<String, Object>> rxBusObservable = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_selector_main_file);
        getActivityComponent().inject(this);

        initData();

        ButterKnife.bind(this);
        initViews();

        registRxViewsClickEvent();

        registRxBus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBusWithTag.getInstance().unregister(FILE_RXBUS_TAG, rxBusObservable);
    }

    private void initData() {
        Bundle bundle = getIntent().getExtras();
        int maxNum = FileComponent.builder(context).getMaxNum();
        if (maxNum > 0) {
            selectFileMaxNum = maxNum;
        }

        double maxSize = FileComponent.builder(context).getMaxSize();
        if (maxSize > 0) {
            singleFileMaxSize = maxSize;
        }

        webId = BundleUtil.getIntegerWithKey(bundle, KEY_WEBID);
    }

    private void initViews(){
        tvSelectorSize.setText(getString(R.string.size, "0B"));
        tvSend.setText(getString(R.string.send, "0"));

        if (FileComponent.builder(context).isShowRecent()) {
            fileSourceFragments.add(new FileRecentMainFragment());
            topRbRecent.setVisibility(View.VISIBLE);
        } else {
            topRbRecent.setVisibility(View.GONE);
        }

        if (FileComponent.builder(context).isShowLocal()) {
            fileSourceFragments.add(new FileLocalMainFragment());
            topRbLocal.setVisibility(View.VISIBLE);
        } else {
            topRbLocal.setVisibility(View.GONE);
        }

        if (topRbRecent.getVisibility() == View.GONE && topRbLocal.getVisibility() == View.GONE) {
            ToastUtil.displayToastShort(context, "未设置数据类型");
        }

        FileTabPagerAdapter fileTabPagerAdapter = new FileTabPagerAdapter(getSupportFragmentManager(), null, fileSourceFragments);
        fileSelectorMainViewpager.setAdapter(fileTabPagerAdapter);

        mainTopRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (checkedId == topRbRecent.getId()) {
                    fileSelectorMainViewpager.setCurrentItem(group.indexOfChild(topRbRecent));
                } else if (checkedId == topRbLocal.getId()) {
                    fileSelectorMainViewpager.setCurrentItem(group.indexOfChild(topRbLocal));
                }
            }
        });

        //默认选中
        //qq：可以使用sp进行用户选择保存
        fileSelectorMainViewpager.setCurrentItem(1);
        topRbLocal.setChecked(true);
    }

    private void registRxViewsClickEvent() {
        //返回
        RxView.clicks(iftvCancel)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
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
                        selectedFileInfoList.clear();

                        finish();
                    }
                });

        //预览
        RxView.clicks(tvPreview)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Void>bindToLifecycle())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Void aVoid) {
                        //预览组件
                        List<String> imagePathList = getImagePathList(selectedFileInfoList);

                        if (imagePathList.size() == 0) {
                            return;
                        }

                        Bundle bundle =  new Bundle();
                        bundle.putStringArrayList(ImageShowActivity.BUNDLE_IMAGE_LIST, (ArrayList<String>) imagePathList);
                        bundle.putInt(ImageShowActivity.BUNDLE_SELECTED_INDEX, 0);
                        ActivityUtil.changeActivity(FileSelectorMainActivity.this,
                                ImageShowActivity.class, bundle, false);
                    }
                });

        //发送
        RxView.clicks(tvSend)
                .throttleFirst(500,TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Void>bindToLifecycle())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Void aVoid) {
                        //发送，回调
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();

                        bundle.putSerializable(BUNDLE_SELECTED_FILE_LIST, (Serializable) selectedFileInfoList);
                        intent.putExtras(bundle);

                        setResult(RESULT_OK,intent);

                        // 同时发送一个通知，避免有的实现不是在activity里
                        RxBusWithTag.getInstance().send("selectFileCompleted", selectedFileInfoList);

                        FileSelectorMainActivity.this.finish();
                    }
                });
    }

    private List<String> getImagePathList(List<FileInfo> fileInfoList) {
        List<String> imagePathList = new ArrayList<>();

        for (FileInfo fileInfo : fileInfoList) {
            if (fileInfo.getFileType() == FileTypeEnum.TYPE_IMAGE.getFileType()
                    && fileInfo.getFileLocalPath().length() > 0) {
                imagePathList.add(fileInfo.getFileLocalPath());
            }
        }

        return imagePathList;
    }

    private void registRxBus() {
        rxBusObservable = RxBusWithTag.getInstance().register(FILE_RXBUS_TAG);

        rxBusObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Map<String, Object> objectMap) {
                        if (null != objectMap) {
                            FileInfo fileInfo = (FileInfo) objectMap.get(KEY_FILE_INFO);

                            //1：处理文件
                            handleFile(fileInfo);

                            //2：刷新ui
                            updateViews();
                        }
                    }
                });
    }

    private void handleFile(FileInfo fileInfo) {
        synchronized (selectedFileInfoList) {
            if (fileInfo.fileSize >= singleFileMaxSize) {
                ToastUtil.displayToastShort(context, "选择文件不能大于" + FileUtil.getFileSzie(singleFileMaxSize));
            } else {
                if (selectedFileInfoList.contains(fileInfo)) {
                    selectedFileInfoList.remove(fileInfo);
                } else {
                    if (selectedFileInfoList.size() >= selectFileMaxNum) {
                        //选择的文件达到上限
                        //FIXME 工具类瑕疵。狂点，toast会显示很长时间，交互不好
                        ToastUtil.displayToastShort(context, StringUtil.connectStrings("你最多只能选择", selectedFileInfoList.size() + "", "个文件"));
                    } else {
                        selectedFileInfoList.add(fileInfo);
                    }
                }
            }
        }
    }

    private long calculateFileSize() {
        long count = 0L;

        for (int i = 0; i < selectedFileInfoList.size(); i++) {
            count = count + selectedFileInfoList.get(i).getFileSize();
        }

        return count;
    }

    private List<FileInfo> filterPhotos() {
        List<FileInfo> images = new ArrayList<>();
        synchronized (selectedFileInfoList) {
            for (int i = 0; i < selectedFileInfoList.size(); i ++) {
                FileInfo fileInfo = selectedFileInfoList.get(i);
                if (fileInfo.fileType == FileTypeEnum.TYPE_IMAGE.getFileType()) {
                    images.add(fileInfo);
                }
            }
        }

        return images;
    }

    private void updateViews() {
        //过滤图片
        List<FileInfo> images = new ArrayList<>();
        images.addAll(filterPhotos());
        if (images.size() == 0) {
            tvPreview.setVisibility(View.GONE);
            tvPreview.setBackgroundResource(R.drawable.shape_tv_send);
            tvPreview.setTextColor(getResources().getColor(R.color.normal_minor_text));
            tvPreview.setEnabled(false);
        } else {
            tvPreview.setVisibility(View.VISIBLE);
            tvPreview.setBackgroundResource(R.drawable.shape_tv_send_green);
            tvPreview.setTextColor(getResources().getColor(R.color.white));
            tvPreview.setEnabled(true);
        }

        //发送按钮
        if (selectedFileInfoList.size() == 0) {
            tvSend.setEnabled(false);
        } else {
            tvSend.setEnabled(true);
        }
        tvSend.setText(getString(R.string.send, "" + selectedFileInfoList.size()));

        //size
        long size = calculateFileSize();
        if (selectedFileInfoList.size() == 0) {
            tvSelectorSize.setText(getString(R.string.size, "0B"));
            tvSend.setTextColor(ContextCompat.getColor(context, R.color.normal_minor_btn));
        } else  {
            tvSelectorSize.setText(getString(R.string.size, FileUtil.FormetFileSize(size)));
            tvSend.setTextColor(ContextCompat.getColor(context, R.color.white));
        }

        refreshFileListViews();
    }

    private void refreshFileListViews() {
        if (fileSourceFragments != null && fileSourceFragments.size() > 0) {
            int fragmentIndex = fileSelectorMainViewpager.getCurrentItem();
            Fragment fragment = fileSourceFragments.get(fragmentIndex);
            if (fragment instanceof FileRecentMainFragment) {
                ((FileRecentMainFragment) fragment).refreshFragmentWithOperType(null, FileBaseFragment.FLAG_OPER_TYPE_DEFAULT);
            } else if (fragment instanceof FileLocalMainFragment) {
                ((FileLocalMainFragment) fragment).refreshFragmentWithOperType(null, FileBaseFragment.FLAG_OPER_TYPE_DEFAULT);
            }
        }
    }
}
