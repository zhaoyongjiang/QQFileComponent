package cn.xxt.file.ui.manager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;

import com.jakewharton.rxbinding.view.RxView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.xxt.commons.injection.ActivityContext;
import cn.xxt.commons.util.RxBusWithTag;
import cn.xxt.commons.util.ToastUtil;
import cn.xxt.commons.widget.AlwaysMarqueeTextView;
import cn.xxt.commons.widget.IconFontTextView;
import cn.xxt.file.R;
import cn.xxt.file.R2;
import cn.xxt.file.internal.domain.FileDownloadStatusEnum;
import cn.xxt.file.internal.domain.FileInfo;
import cn.xxt.file.internal.domain.FileTypeEnum;
import cn.xxt.file.ui.base.FileBaseActivity;
import cn.xxt.file.util.FileUtil;
import cn.xxt.webview.ui.fileOpen.X5FileOpenUtil;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by zyj on 2017/9/2.
 */

public class FileOpenActivity extends FileBaseActivity {
    /** fragment的index标志 */
    private final static int FLAG_FILE_DOWNLOAD = 0;
    private final static int FLAG_FILE_UNVAILABLE = 1;
    private final static int FLAG_AUDIO_PLAY = 2;
    private final static int FLAG_FILE_OPEN = 3;
    private final static int FLAG_APK_INSTALL = 4;

    public static final String BUNDLE_FILEINFO = "BUNDLE_FILEINFO";
    public static final String BUNDLE_DOWNLOAD_MANAGER = "BUNDLE_DOWNLOAD_MANAGER";

    @BindView(R2.id.iftv_top_left)
    IconFontTextView iftvBack;

    @BindView(R2.id.tv_top_title)
    AlwaysMarqueeTextView amtvTitle;

    @Inject
    @ActivityContext
    Context context;

    private FileInfo fileInfo = null;

    /** fragment管理器 */
    private FragmentManager fragmentManager;

    private FileOpenDownloadFragment fileOpenDownloadFragment;

    private FileOpenAudioPlayFragment fileOpenAudioPlayFragment;

    private FileOpenUnVailableFragment fileOpenUnVailableFragment;

    private FileOpenApkInstallFragment fileOpenApkInstallFragment;

    private FileOpenFragment fileOpenFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_open_file);
        getActivityComponent().inject(this);

        fragmentManager = getSupportFragmentManager();

        ButterKnife.bind(this);

        initData();
        initViews();

        registRxViewsClickEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 是否触发按键为back键
        if (keyCode == KeyEvent.KEYCODE_BACK
                || keyCode == KeyEvent.KEYCODE_HOME) {
            onBackPressed();
            return true;
        } else {
            // 如果不是back键正常响应
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //点击返回键
        sendRxBusTransmitDownloader();

        return;
    }

    private void initData() {
        //获取数据
        //TODO test from mc
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            fileInfo = (FileInfo) bundle.getSerializable(BUNDLE_FILEINFO);
        }
    }

    private void initViews(){
        if (amtvTitle != null) {
            amtvTitle.setText(fileInfo.fileName);
        }
        handleFragmentToShow(fileInfo);
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
                        //界面返回。
                        sendRxBusTransmitDownloader();

                        finish();

                    }
                });
    }

    /**
     * 根据文件实体，处理展示那个fragment
     * @param file
     */
    public void handleFragmentToShow(FileInfo file) {
        this.fileInfo = file;

        FileUtil.analyzeFileTypeWithFileUrl(this.fileInfo);

        if (fileInfo == null) {
            ToastUtil.displayToastShort(context, "文件异常");
            setTabSelection(FLAG_FILE_UNVAILABLE);
        } else {
            if (fileInfo.downloadStatus == FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_NOT_YET.getFileDownloadStatus()) {
                //未下载
                setTabSelection(FLAG_FILE_DOWNLOAD);
            } else if (fileInfo.downloadStatus == FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_DOWNLOADED.getFileDownloadStatus()
                    || fileInfo.downloadStatus == FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_READY.getFileDownloadStatus()) {
                if (FileUtil.isFileExits(fileInfo)) {
                    //文件存在
                    if (fileInfo.fileType == FileTypeEnum.TYPE_AUDIO.getFileType()) {
                        setTabSelection(FLAG_AUDIO_PLAY);
                    } else if (fileInfo.fileType == FileTypeEnum.TYPE_OTHER_APK.getFileType()) {
                        //安装包
                        setTabSelection(FLAG_APK_INSTALL);
                    } else {
                        //其他可打开的文件：现阶段用腾讯x5内核打开
                        //rar，zip，文档，视频
                        openfile(fileInfo);
                    }
                } else if (fileInfo.getFileServerPath() != null
                        && fileInfo.getFileServerPath().length() > 0) {
                    //未下载，显示下载fragment
                    setTabSelection(FLAG_FILE_DOWNLOAD);
                } else {
                    //失效，显示失效fragment
                    setTabSelection(FLAG_FILE_UNVAILABLE);
                }
            } else if (fileInfo.downloadStatus == FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_DOWNLOADING.getFileDownloadStatus()){
                //下载中，显示下载fragment
                setTabSelection(FLAG_FILE_DOWNLOAD);
            }
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
                case FLAG_FILE_DOWNLOAD:
                    if (fileOpenDownloadFragment == null) {
                        fileOpenDownloadFragment = new FileOpenDownloadFragment();
                        fileOpenDownloadFragment.initData(fileInfo);
                        transaction.add(R.id.id_content, fileOpenDownloadFragment);
                    } else {
                        transaction.show(fileOpenDownloadFragment);
                    }
                    break;
                case FLAG_FILE_UNVAILABLE:
                    if (fileOpenUnVailableFragment == null) {
                        fileOpenUnVailableFragment = new FileOpenUnVailableFragment();
                        fileOpenUnVailableFragment.initData(fileInfo);
                        transaction.add(R.id.id_content, fileOpenUnVailableFragment);
                    } else {
                        transaction.show(fileOpenUnVailableFragment);
                    }
                    break;
                case FLAG_AUDIO_PLAY:
                    if (fileOpenAudioPlayFragment == null) {
                        fileOpenAudioPlayFragment = new FileOpenAudioPlayFragment();
                        fileOpenAudioPlayFragment.initData(fileInfo);
                        transaction.add(R.id.id_content, fileOpenAudioPlayFragment);
                    } else {
                        transaction.show(fileOpenAudioPlayFragment);
                    }
                    break;
                case FLAG_APK_INSTALL:
                    if (fileOpenApkInstallFragment == null) {
                        fileOpenApkInstallFragment = new FileOpenApkInstallFragment();
                        fileOpenApkInstallFragment.initData(fileInfo);
                        transaction.add(R.id.id_content, fileOpenApkInstallFragment);
                    } else {
                        transaction.show(fileOpenApkInstallFragment);
                    }
                    break;
                case FLAG_FILE_OPEN:
                    if (fileOpenFragment == null) {
                        fileOpenFragment = new FileOpenFragment();
                        transaction.add(R.id.id_content, fileOpenFragment);
                    } else {
                        transaction.show(fileOpenFragment);
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
            if (fileOpenDownloadFragment != null) {
                transaction.hide(fileOpenDownloadFragment);
            }
            if (fileOpenAudioPlayFragment != null) {
                transaction.hide(fileOpenAudioPlayFragment);
            }
            if (fileOpenUnVailableFragment != null) {
                transaction.hide(fileOpenUnVailableFragment);
            }
            if (fileOpenApkInstallFragment != null) {
                transaction.hide(fileOpenApkInstallFragment);
            }
            if (fileOpenFragment != null) {
                transaction.hide(fileOpenFragment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件打开
     * @param fileInfo
     */
    private void openfile(FileInfo fileInfo) {
        //关闭界面，打开文件

        final X5FileOpenUtil fileOpenManager = new X5FileOpenUtil(context,
                fileInfo.getFileLocalPath(), fileInfo.getFileName(), FileUtil.matchFileType2X5FileType(fileInfo));
        fileOpenManager.openFile(new X5FileOpenUtil.OnFileOpenListener() {
            @Override
            public void openResult(boolean result) {
                ((Activity)context).finish();
            }
        });
    }

    /**
     * 点击返回：发送通知转交下载器
     */
    private void sendRxBusTransmitDownloader() {
        Map<String, Object> rxMap = new HashMap<>();
        rxMap.put(BUNDLE_FILEINFO, fileInfo);
//        rxMap.put(BUNDLE_DOWNLOAD_MANAGER, fileDownloadManager);
        RxBusWithTag.getInstance().send(String.valueOf(fileInfo.getFileId()), rxMap);
    }
}
