package cn.xxt.file.ui.manager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import cn.xxt.commons.injection.ActivityContext;
import cn.xxt.commons.util.download.DownloadFileUtil;
import cn.xxt.commons.widget.IconFontTextView;
import cn.xxt.file.R;
import cn.xxt.file.R2;
import cn.xxt.file.internal.data.local.FileDb;
import cn.xxt.file.internal.domain.FileDownloadStatusEnum;
import cn.xxt.file.internal.domain.FileInfo;
import cn.xxt.file.ui.base.FileBaseFragment;
import cn.xxt.file.util.FileDownloadManager;
import cn.xxt.file.util.FileUtil;
import cn.xxt.file.util.TaskDispatcher;
import cn.xxt.file.util.TransmitClass;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by zyj on 2017/9/9.
 */

public class FileOpenDownloadFragment extends FileBaseFragment {

    @BindView(R2.id.iftv_file_icon)
    IconFontTextView iftvFileIcon;

    @BindView(R2.id.tv_file_name)
    TextView tvFileName;

    /** 比如：大文件可在下载前用"在线预览"提前查阅 */
    @BindView(R2.id.tv_file_status_description)
    TextView tvFileStatusDescription;

    @BindView(R2.id.btn_download)
    Button btnDownload;

    @BindView(R2.id.btn_ready_download)
    Button btnReadyDownload;

    @BindView(R2.id.ll_progress)
    LinearLayout llProgress;

    @BindView(R2.id.pb_download)
    ProgressBar pbDownload;

    @BindView(R2.id.iftv_cancel)
    IconFontTextView iftvCancel;

    @Inject
    @ActivityContext
    Context context;

    private FileDownloadManager fileDownloadManager;
    private FileInfo fileInfo;

    public void setFileSavePath(String fileSavePath) {
        this.fileSavePath = fileSavePath;
    }

    public void setFileSaveName(String fileSaveName) {
        this.fileSaveName = fileSaveName;
    }

    private String fileSavePath;
    private String fileSaveName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        injectThis();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.fragment_file_open_download_file;
    }

    @Override
    public void initView() {
        initViews();
        registRxViewsClickEvent();
    }

    /**
     * 开发给openActivity调用。传进来：下载管理器，文件实体
     * @param fileInfo
     */
    public void initData(FileInfo fileInfo) {
        this.fileDownloadManager = TransmitClass.fileDownloadManagerMap.get(fileInfo.getFileId());
        this.fileInfo = fileInfo;
    }

    private void initViews() {
        //图标
        int iconCode = FileUtil.getFileIconCodeByFileType(context, fileInfo.fileType);
        int iconColor = FileUtil.getFileIconColorByFileType(context, fileInfo.fileType);
        iftvFileIcon.setText(iconCode);
        iftvFileIcon.setTextColor(ContextCompat.getColor(context, iconColor));

        //文件名称
        String fileName = fileInfo.fileName;
        tvFileName.setText(fileName);

        //描述
        handleFileDescription(this.fileInfo);

        //文件大小
        String fileSize = FileUtil.FormetFileSize(fileInfo.getFileSize());
        btnDownload.setText(getString(R.string.down_size, fileSize));

        refreshViews(fileInfo);


        //处理下载逻辑
        handleFileDownload();
    }

    /**
     * 文件下载界面：描述
     *
     * 暂时先固定显示：请先下载文件
     *
     * 根据不同文件，显示不同的说明文案
     *
     * @param fileInfo
     */
    private void handleFileDescription(FileInfo fileInfo) {
        if (tvFileStatusDescription != null) {
            if (fileInfo == null) {
                tvFileStatusDescription.setVisibility(View.GONE);
            } else {
                tvFileStatusDescription.setVisibility(View.VISIBLE);
            }
        }
    }

    private void registRxViewsClickEvent() {
        //下载
        RxView.clicks(btnDownload)
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
                        //下载组件
                        fileInfo.setDownloadStatus(FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_READY.getFileDownloadStatus());
                        refreshViews(fileInfo);

                        TaskDispatcher taskDispatcher = TaskDispatcher.getInstance();
                        taskDispatcher.doTask(new Runnable() {
                            @Override
                            public void run() {
                                Observable.just(1)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Subscriber<Integer>() {
                                            @Override
                                            public void onCompleted() {

                                            }

                                            @Override
                                            public void onError(Throwable e) {

                                            }

                                            @Override
                                            public void onNext(Integer integer) {
                                                //下载
                                                fileDownloadManager = downloadFile();
                                                TransmitClass.fileDownloadManagerMap.put(fileInfo.getFileId(), fileDownloadManager);
                                            }
                                        });
                            }
                        });
                    }
                });

        //取消下载
        RxView.clicks(iftvCancel)
                .throttleFirst(500,TimeUnit.MILLISECONDS)
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
                        //TODO  如果直接杀掉线程，测试是否导致下载崩溃
                        //取消下载subscribetion

                        //删除文件
                        DownloadFileUtil.deleteFile(fileSavePath);
                    }
                });
    }

    /**
     * 处理文件下载：在activity中应处理过逻辑，如果是下载中或未下载，显示该fragment。
     *
     * 在initviews后，开始处理下载
     */
    private void handleFileDownload() {
        if (fileInfo != null) {
            if (fileDownloadManager != null) {
                //传进来的下载管理器不是空的。说明点击item进来的时候，就是下载中，所以，该处直接更新一下回调监听就行了。
                fileDownloadManager.updateDownLoadListener(new FileDownloadManager.OnFileDownloadListener() {
                    @Override
                    public void onDownloadBegin(long fileId, String fileLocalPath) {

                    }

                    @Override
                    public void onDownloadProgress(int percent) {
                        pbDownload.setProgress(percent);
                    }

                    @Override
                    public void onDownloadPause(int percent) {

                    }

                    @Override
                    public void onDownloadComplete(String url, long fileId, String localPath, String fileSaveName) {
                        dispatchCompleteToMainThread(fileInfo, url, localPath, fileSaveName);
                    }

                    @Override
                    public void onDownloadError(String url) {
                        dispatchErrorToMainThread(fileInfo, url);
                    }
                });
            }
        }
    }

    private FileDownloadManager downloadFile() {
        fileDownloadManager = new FileDownloadManager(fileInfo.getFileId(), null, fileInfo.getFileName(), fileInfo.fileServerPath, context);

        fileDownloadManager.downloadFile(new FileDownloadManager.OnFileDownloadListener() {
            @Override
            public void onDownloadBegin(long fileId, String fileLocalPath) {
                dispatchBeginToMainThread(fileInfo, fileLocalPath);
            }

            @Override
            public void onDownloadProgress(int percent) {
                dispatchProgressToMainThread(percent);
            }

            @Override
            public void onDownloadPause(int percent) {

            }

            @Override
            public void onDownloadComplete(String url, long fileId, String fileLocalPath, String fileSaveName) {
                dispatchCompleteToMainThread(fileInfo, url, fileLocalPath, fileSaveName);
            }

            @Override
            public void onDownloadError(String url) {
                dispatchErrorToMainThread(fileInfo, url);
            }
        });

        return fileDownloadManager;
    }

    private void updateFileDownloadStatus(final FileInfo fileInfo) {
        Observable.just(1)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        FileDb fileDb = FileDb.getInstance(context);
                        fileDb.updateFileDownloadStatus(0, fileInfo.getFileId(), fileInfo.getDownloadStatus());
                    }
                });
    }

    private void injectThis(){
        Activity activity = getActivity();
        if (activity instanceof FileOpenActivity) {
            ((FileOpenActivity) activity).getActivityComponent().inject(this);
        }
    }

    private void dispatchBeginToMainThread(final FileInfo fileInfo
            , final String fileLocalPath) {
        Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        //开始下载
                        setFileSavePath(fileLocalPath);

                        fileInfo.setDownloadStatus(FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_DOWNLOADING.getFileDownloadStatus());
                        updateFileDownloadStatus(fileInfo);

                        refreshViews(fileInfo);
                    }
                });
    }

    private void dispatchProgressToMainThread(final int percent) {
        Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        pbDownload.setProgress(percent);
                    }
                });
    }

    private void dispatchCompleteToMainThread(final FileInfo fileInfo
            , final String url
            , final String fileLocalPath
            , final String fileSaveName) {
        Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        fileInfo.setFileLocalPath(fileLocalPath);
                        fileInfo.setFileName(fileSaveName);
                        fileInfo.setDownloadStatus(FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_DOWNLOADED.getFileDownloadStatus());
                        updateFileDownloadStatus(fileInfo);

                        fileDownloadOverHandle(fileInfo);

                        refreshViews(fileInfo);

                        //下载完成：切换到对应的fragment
                        if (getActivity() instanceof FileOpenActivity) {
                            ((FileOpenActivity) getActivity()).handleFragmentToShow(fileInfo);
                        }
                    }
                });
    }

    private void dispatchErrorToMainThread(final FileInfo fileInfo
            , final String url) {
        Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        fileInfo.setDownloadStatus(FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_NOT_YET.getFileDownloadStatus());

                        updateFileDownloadStatus(fileInfo);

                        refreshViews(fileInfo);

                        fileDownloadOverHandle(fileInfo);
                    }
                });
    }

    private void fileDownloadOverHandle(FileInfo fileInfo) {
        if (fileInfo != null && fileInfo.getFileServerPath() != null
                && fileInfo.getFileServerPath().length() > 0) {
            TransmitClass.fileDownloadManagerMap.remove(fileInfo.getFileId());

            //并发释放信号量,允许下一个下载线程启动
            TaskDispatcher.getInstance().releaseSemaphore();
        }
    }

    private void refreshViews(FileInfo fileInfo) {
        //控件状态
        btnDownload.setVisibility(View.GONE);
        llProgress.setVisibility(View.GONE);
        btnReadyDownload.setVisibility(View.GONE);

        if (fileInfo.downloadStatus
                == FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_DOWNLOADING.getFileDownloadStatus()) {
            llProgress.setVisibility(View.VISIBLE);
        } else if (fileInfo.downloadStatus
                == FileDownloadStatusEnum.FILE_DOWNLOAD_STATUS_NOT_YET.getFileDownloadStatus()){
            if (!FileUtil.isFileExits(fileInfo) && fileInfo.getFileServerPath().length() > 0) {
                btnDownload.setVisibility(View.VISIBLE);
            }
        } else {
            btnReadyDownload.setVisibility(View.VISIBLE);
        }
    }
}
