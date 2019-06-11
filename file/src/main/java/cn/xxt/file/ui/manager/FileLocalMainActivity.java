package cn.xxt.file.ui.manager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.xxt.commons.injection.ActivityContext;
import cn.xxt.commons.util.BundleUtil;
import cn.xxt.commons.util.RxBusWithTag;
import cn.xxt.commons.util.StringUtil;
import cn.xxt.commons.util.ToastUtil;
import cn.xxt.commons.widget.AlwaysMarqueeTextView;
import cn.xxt.commons.widget.IconFontTextView;
import cn.xxt.file.R;
import cn.xxt.file.R2;
import cn.xxt.file.api.FileComponent;
import cn.xxt.file.api.FileComponentCommonValue;
import cn.xxt.file.internal.domain.FileInfo;
import cn.xxt.file.ui.base.FileBaseActivity;
import cn.xxt.file.ui.base.FileBaseFragment;
import cn.xxt.file.ui.fileFragment.FileLocalMainFragment;
import cn.xxt.file.util.FileUtil;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static cn.xxt.file.api.FileComponentCommonValue.KEY_WEBID;

/**
 * Created by zyj on 2017/8/16.
 */

public class FileLocalMainActivity extends FileBaseActivity {

    //在activity中通过控制该状态，刷新fragment界面时候，
    // fragment会通过该状态，给adapter设置不同的ItemUiType（ItemUiTypeEnum）。
    // 然后根据ItemUiType可以区分是否是编辑状态。
    public boolean isEditStatus() {
        return isEditStatus;
    }

    private boolean isEditStatus = false;

    @Inject
    @ActivityContext
    Context context;

    @BindView(R2.id.tv_top_title)
    AlwaysMarqueeTextView tvTopTitle;

    @BindView(R2.id.iftv_top_left)
    IconFontTextView iftvBack;

    @BindView(R2.id.iftv_top_edit)
    IconFontTextView iftvEdit;

    @BindView(R2.id.iftv_top_cancel)
    IconFontTextView iftvCancel;

    @BindView(R2.id.tv_all)
    TextView tvSD;

    @BindView(R2.id.ll_bottom_oper)
    LinearLayout llOper;

    @BindView(R2.id.iftv_delete)
    IconFontTextView iftvDelete;

    @BindView(R2.id.iftv_share)
    IconFontTextView iftvShare;

    @BindView(R2.id.iftv_download)
    IconFontTextView iftvDownload;

    /** fragment的index标志 */
    private final static int FRAGMENT_INDEX_LIST = 0;

    /** fragment管理器 */
    private FragmentManager fragmentManager;
    /** FileLocalMainFragment */
    private FileLocalMainFragment fileLocalMainFragment;

    private Observable<Map<String, Object>> rxBusObservable = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_local_main_file);
        getActivityComponent().inject(this);

        initData();

        fragmentManager = getSupportFragmentManager();

        ButterKnife.bind(this);

        registRxViewsClickEvent();

        registRxBus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTabSelection(FRAGMENT_INDEX_LIST);

        refreshBottomView();
        refreshTopRightView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBusWithTag.getInstance().unregister(FILE_RXBUS_TAG, rxBusObservable);
    }

    private void initData() {
        Bundle bundle = getIntent().getExtras();

        int maxNum = FileComponent.builder(context).getMaxNum();
        webId = BundleUtil.getIntegerWithKey(bundle, KEY_WEBID);

        if (maxNum > 0) {
            selectFileMaxNum = maxNum;
        }
    }

    /**
     * 切换fragment
     * @param index 按钮标记位
     */
    private void setTabSelection(int index) {
        try {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            hideFragments(transaction);
            switch (index) {
                case FRAGMENT_INDEX_LIST:
                    if (fileLocalMainFragment == null) {
                        fileLocalMainFragment = new FileLocalMainFragment();
                        transaction.replace(R.id.fl_content, fileLocalMainFragment);
                    } else {
                        transaction.show(fileLocalMainFragment);
                    }
                    break;
                default:
                    break;
            }
            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 切换事件前，先隐藏原有fragment
     * @param transaction fragment任务
     */
    private void hideFragments(FragmentTransaction transaction) {
        try {
            if (fileLocalMainFragment != null) {
                transaction.hide(fileLocalMainFragment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registRxViewsClickEvent() {
        //返回
        RxView.clicks(iftvBack)
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

        //编辑
        RxView.clicks(iftvEdit)
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
                        //点击编辑
                        isEditStatus = !isEditStatus;
                        fileLocalMainFragment.refreshFragmentWithOperType(null, FileBaseFragment.FLAG_OPER_TYPE_DEFAULT);

                        refreshTopRightView();
                        refreshBottomView();
                    }
                });

        //取消
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

                    }

                    @Override
                    public void onNext(Void aVoid) {
                        //点击取消
                        isEditStatus = !isEditStatus;
                        fileLocalMainFragment.refreshFragmentWithOperType(null, FileBaseFragment.FLAG_OPER_TYPE_DEFAULT);

                        refreshTopRightView();
                        refreshBottomView();
                    }
                });

        //删除
        RxView.clicks(iftvDelete)
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
                        //点击删除
                        deleteFileList(selectedFileInfoList);

                        //刷新界面
                        isEditStatus = !isEditStatus;
                        fileLocalMainFragment.refreshFragmentWithOperType(selectedFileInfoList, FileBaseFragment.FLAG_OPER_TYPE_DELETE);

                        selectedFileInfoList.clear();

                        refreshTopRightView();
                        refreshBottomView();
                    }
                });

        //分享
        RxView.clicks(iftvShare)
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
                        //点击分享
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();

                        ArrayList list = new ArrayList<>();
                        list.add(selectedFileInfoList);
                        bundle.putParcelableArrayList(FileComponentCommonValue.BUNDLE_SELECTED_FILE_LIST, list);

                        intent.putExtras(bundle);

//                        setResult(RESULT_OK,intent);
//                        FileLocalMainActivity.this.finish();
                    }
                });
    }

    private void refreshTopRightView() {
        if (isEditStatus) {
            iftvCancel.setVisibility(View.VISIBLE);
            iftvEdit.setVisibility(View.GONE);
        } else {
            iftvCancel.setVisibility(View.GONE);
            iftvEdit.setVisibility(View.VISIBLE);
        }
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

                            //1：处理文件
                            handleFile(objectMap);

                            //2：刷新ui
                            refreshBottomView();
                            fileLocalMainFragment.refreshFragmentWithOperType(null, FileBaseFragment.FLAG_OPER_TYPE_DEFAULT);
                        }
                    }
                });
    }

    private void refreshBottomView() {
        if (isEditStatus) {
            tvSD.setVisibility(View.GONE);
            llOper.setVisibility(View.VISIBLE);
        } else {
            tvSD.setVisibility(View.VISIBLE);
            llOper.setVisibility(View.GONE);

            selectedFileInfoList.clear();
        }
        refreshBottomOperView();
    }

    private void refreshBottomOperView() {
        if (selectedFileInfoList != null && selectedFileInfoList.size() > 0) {
            iftvDelete.setEnabled(true);
//            iftvDelete.setTextColor(ContextCompat.getColor(context, R.color.md_grey_800));

            iftvShare.setEnabled(true);
//            iftvShare.setTextColor(ContextCompat.getColor(context, R.color.md_grey_800));
        } else {
            iftvDelete.setEnabled(false);
//            iftvDelete.setTextColor(ContextCompat.getColor(context, R.color.color_unknown));

            iftvShare.setEnabled(false);
//            iftvShare.setTextColor(ContextCompat.getColor(context, R.color.color_unknown));
        }
    }

    /**
     *
     * 处理列表中文件选择操作
     *
     * 是通过rxbus传过来的：操作类型（选中，取消），文件实体
     *
     * 然后：处理该界面持有的选择的数据源
     *
     * @param objectMap
     */
    private void handleFile(Map<String, Object> objectMap) {
        synchronized (selectedFileInfoList) {
            FileInfo fileInfo = (FileInfo) objectMap.get(KEY_FILE_INFO);
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

    private void deleteFileList(List<FileInfo> fileInfoList) {
        if (fileInfoList != null && fileInfoList.size() > 0) {
            for (FileInfo fileInfo : fileInfoList) {
                if (FileUtil.isFileExits(fileInfo)) {
                    FileUtil.deleteFile(fileInfo.getFileLocalPath());
                }
            }
        }
    }
}
