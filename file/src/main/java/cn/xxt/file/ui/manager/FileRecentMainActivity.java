package cn.xxt.file.ui.manager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

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
import cn.xxt.file.internal.data.local.FileDb;
import cn.xxt.file.internal.domain.FileDownloadStatusEnum;
import cn.xxt.file.internal.domain.FileInfo;
import cn.xxt.file.ui.base.FileBaseActivity;
import cn.xxt.file.ui.base.FileBaseFragment;
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

public class FileRecentMainActivity extends FileBaseActivity {
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
    private FileRecentMainFragment fileRecentMainFragment;

    private Observable<Map<String, Object>> rxBusObservable = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_recent_main_file);
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
                    if (fileRecentMainFragment == null) {
                        fileRecentMainFragment = new FileRecentMainFragment();
                        transaction.replace(R.id.fl_content, fileRecentMainFragment);
                    } else {
                        transaction.show(fileRecentMainFragment);
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
            if (fileRecentMainFragment != null) {
                transaction.hide(fileRecentMainFragment);
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
                        fileRecentMainFragment.refreshFragmentWithOperType(null, FileBaseFragment.FLAG_OPER_TYPE_DEFAULT);

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
                        fileRecentMainFragment.refreshFragmentWithOperType(null, FileBaseFragment.FLAG_OPER_TYPE_DEFAULT);

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
                        try {
                            //1批量删除：
                            deleteFileList(selectedFileInfoList);

                            //2刷新界面
                            isEditStatus = !isEditStatus;
                            fileRecentMainFragment.refreshFragmentWithOperType(selectedFileInfoList, FileBaseFragment.FLAG_OPER_TYPE_DELETE);

                            selectedFileInfoList.clear();

                            refreshTopRightView();
                            refreshBottomView();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
                        //TODO 转发，，可通过setresult回调。。要判断信息对应的本地文件是否存在
                        if (filterFileExits(selectedFileInfoList).size() > 0) {
                            ToastUtil.displayToastShort(context, "分享");
                            Intent intent = new Intent();
                            Bundle bundle = new Bundle();

                            ArrayList list = new ArrayList<>();
                            list.add(selectedFileInfoList);
                            bundle.putParcelableArrayList(BUNDLE_SELECTED_FILE_LIST, list);

                            intent.putExtras(bundle);

//                            setResult(RESULT_OK,intent);
//                            FileRecentMainActivity.this.finish();
                        } else {
                        }
                    }
                });

        //下载
        RxView.clicks(iftvDownload)
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
                        //点击下载
                        //刷新界面
                        isEditStatus = !isEditStatus;

                        if (filterFileUnDownload(selectedFileInfoList).size() > 0) {
                            ToastUtil.displayToastShort(context, "下载");
                            fileRecentMainFragment.refreshFragmentWithOperType(selectedFileInfoList, FileBaseFragment.FLAG_OPER_TYPE_DOWNLOAD);
                        } else {

                        }

                        selectedFileInfoList.clear();

                        refreshTopRightView();
                        refreshBottomView();
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
                            FileInfo fileInfo = (FileInfo) objectMap.get(KEY_FILE_INFO);

                            //1：处理文件
                            handleFile(fileInfo);

                            //2：刷新ui
                            refreshBottomOperView();
                            fileRecentMainFragment.refreshFragmentWithOperType(null, FileBaseFragment.FLAG_OPER_TYPE_DEFAULT);
                        }
                    }
                });
    }

    private void refreshBottomView() {
        if (isEditStatus) {
            llOper.setVisibility(View.VISIBLE);
        } else {
            llOper.setVisibility(View.GONE);

            selectedFileInfoList.clear();
        }

        refreshBottomOperView();
    }

    private void refreshBottomOperView() {
        if (selectedFileInfoList != null && selectedFileInfoList.size() > 0) {
            iftvDelete.setEnabled(true);

            //转发，需要选中的文件信息，文件在手机中存在
            if (filterFileExits(selectedFileInfoList).size() > 0) {
                iftvShare.setEnabled(true);
            }

            //下载，只要有未下载的，显示。。下载组件中，再过滤一次
            if (filterFileUnDownload(selectedFileInfoList).size() > 0) {
                iftvDownload.setEnabled(true);
            } else {
                iftvDownload.setEnabled(false);
            }
        } else {
            iftvDelete.setEnabled(false);

            iftvShare.setEnabled(false);

            iftvDownload.setEnabled(false);
        }
    }

    private void handleFile(FileInfo fileInfo) {
        synchronized (selectedFileInfoList) {
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

    private List<FileInfo> filterFileExits(List<FileInfo> fileInfoList) {
        List<FileInfo> exitsFileList = new ArrayList<>();

        for (FileInfo fileInfo : fileInfoList) {
            if (FileUtil.isFileExits(fileInfo)) {
                exitsFileList.add(fileInfo);
            }
        }

        return exitsFileList;
    }

    private List<FileInfo> filterFileUnDownload(List<FileInfo> fileInfoList) {
        List<FileInfo> unDownloadFileList = new ArrayList<>();

        for (FileInfo fileInfo : fileInfoList) {
            if (fileInfo.getDownloadStatus() == FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_NOT_YET.getFileDownloadStatus()) {
                unDownloadFileList.add(fileInfo);
            }
        }

        return unDownloadFileList;
    }

    private void deleteFileList(List<FileInfo> fileInfoList) {
        //1:删除数据库。
        FileDb fileDb = FileDb.getInstance(context);
        boolean result = fileDb.deleteFileByFileIdList(0, getFileIdList(fileInfoList));

        //FIXME：20171110
        // 删除最近中的信息，只删除数据库记录，不删除文件。如果，本机中删除了。
        // 在最近中，会走失效的流程：看本地是否存在，存在显示查看，不存在，看是否有网络url。有显示下载，无。最终显示失效

//        //2:成功
//        if (result) {
//            for (FileInfo fileInfo : fileInfoList) {
//                FileUtil.deleteFile(fileInfo.getFileLocalPath());
//            }
//        } else {
//            //提示
//        }
    }

    private List<Long> getFileIdList(List<FileInfo> fileInfoList) {
        List<Long> fileIdList = new ArrayList<>();

        List<FileInfo> tmpFileInfoList = new ArrayList<>();
        tmpFileInfoList.addAll(fileInfoList);

        for (FileInfo fileInfo : tmpFileInfoList) {
            fileIdList.add(fileInfo.getFileId());
        }

        return fileIdList;
    }
}
