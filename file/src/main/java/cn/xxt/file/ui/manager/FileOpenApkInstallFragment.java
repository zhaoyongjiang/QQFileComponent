package cn.xxt.file.ui.manager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.tbruyelle.rxpermissions.Permission;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import cn.xxt.commons.injection.ActivityContext;
import cn.xxt.commons.widget.IconFontTextView;
import cn.xxt.file.R;
import cn.xxt.file.R2;
import cn.xxt.file.internal.domain.FileInfo;
import cn.xxt.file.ui.base.FileBaseFragment;
import cn.xxt.file.util.FileApkIntallUtil;
import cn.xxt.file.util.FileUtil;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zyj on 2017/11/2.
 */

public class FileOpenApkInstallFragment extends FileBaseFragment {
    @BindView(R2.id.iftv_file_icon)
    IconFontTextView iftvFileIcon;

    @BindView(R2.id.tv_file_name)
    TextView tvFileName;

    /** 比如：大文件可在下载前用"在线预览"提前查阅 */
    @BindView(R2.id.tv_file_size)
    TextView tvFileSize;

    @BindView(R2.id.btn_install)
    Button btnIntall;

    @Inject
    @ActivityContext
    Context context;

    private FileInfo fileInfo = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        injectThis();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.fragment_file_open_apk_install_file;
    }

    @Override
    public void initView() {
        initViews();
        registRxViewsClickEvent();
    }

    /**
     * 供OpenActivity调用。将文件实体传进来
     * @param fileInfo
     */
    public void initData(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    private void initViews() {
        //图标
        int iconCode = FileUtil.getFileIconCodeByFileType(context, fileInfo.fileType);
        int iconColor = FileUtil.getFileIconColorByFileType(context, fileInfo.fileType);
        iftvFileIcon.setText(iconCode);
        iftvFileIcon.setTextColor(ContextCompat.getColor(context, iconColor));

        //文件名称
        //FIXME 文件名是否是 xxx.xxx
        String fileName = fileInfo.fileName;
        tvFileName.setText(fileName);

        //文件大小
        String fileSize = FileUtil.FormetFileSize(fileInfo.getFileSize());
        tvFileSize.setText(getString(R.string.file_size, fileSize));
    }

    private void registRxViewsClickEvent() {
        //安装
        RxView.clicks(btnIntall)
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
                        installApk();
                    }
                });
    }

    private void injectThis(){
        Activity activity = getActivity();
        if (activity instanceof FileOpenActivity) {
            ((FileOpenActivity) activity).getActivityComponent().inject(this);
        }
    }

    private void installApk() {
        new RxPermissions(getActivity())
                .requestEach(
                        Manifest.permission.REQUEST_INSTALL_PACKAGES
                )
                .subscribe(new Action1<Permission>() {
                    @Override
                    public void call(Permission permission) {
                        if(permission.granted){
                            // 获得授权
                            FileApkIntallUtil.intallApk(context, fileInfo.getFileLocalPath());
                        } else {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + context.getPackageName()));
                            startActivityForResult(intent, 1000);
                        }
                    }
                });
    }
}
