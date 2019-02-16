package cn.xxt.webview.ui.fileOpen;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.view.RxView;
import com.tbruyelle.rxpermissions.Permission;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.util.concurrent.TimeUnit;

import cn.xxt.commons.util.BundleUtil;
import cn.xxt.commons.util.FileUtil;
import cn.xxt.commons.util.Md5Util;
import cn.xxt.commons.util.StringUtil;
import cn.xxt.commons.util.ToastUtil;
import cn.xxt.commons.util.download.DownloadFileUtil;
import cn.xxt.commons.util.download.MultiThreadDownload;
import cn.xxt.commons.widget.AlwaysMarqueeTextView;
import cn.xxt.commons.widget.IconFontTextView;
import cn.xxt.commons.widget.XXTDialog;
import cn.xxt.commons.widget.XXTHud;
import cn.xxt.library.ui.base.BaseActivity;
import cn.xxt.webview.R;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zyj on 2018/8/3.
 */
public class DocPreviewActivity extends BaseActivity {

    public static String BUNDLE_TITLE = "BUNDLE_TITLE";
    public static String BUNDLE_FILE_PATH = "BUNDLE_FILE_PATH";

    private final static String FILE_CACHE_DIR = "file/";

    private IconFontTextView ifBack;
    private AlwaysMarqueeTextView tvTitle;
    private TextView tvTopRight;

    private String fileName = "预览";

    private String filePath = "";

    private Context context;

    private DocPreviewFragment docPreviewFragment;

    private DocPreviewListener docPreviewListener;

    public void setDocPreviewListener(DocPreviewListener docPreviewListener) {
        this.docPreviewListener = docPreviewListener;
    }

    public interface DocPreviewListener {
        void downloadStatus(boolean downloadSuccess, String fileUrl, String filePath);

        void openStatus(boolean openResult);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_reader);

        context = DocPreviewActivity.this;

        initData();

        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPermissions();

        if (DocPreviewFragment.isNetWorkFile(filePath)) {
            //网络文件

            String tmpFilePath = getFileLocalPath();
            File file = new File(tmpFilePath);

            if (null != file && file.exists()) {
                showDocPreviewFg();
            } else {
                downloadResFile();

                tvTopRight.setVisibility(View.VISIBLE);
            }
        } else {
            showDocPreviewFg();
        }
    }

    @Override
    protected String getActivityName() {
        return "office文档预览页面";
    }

    /**
     * 返回键点击，返回到手机桌面
     * @param event     拦截事件
     * @return  是否拦截成功
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_UP
                && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            try {
                // 拦截返回键按钮事件，返回到手机桌面
                finish();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return super.dispatchKeyEvent(event);
    }

    private void initData() {
        Bundle bundle = getIntent().getExtras();
        fileName = BundleUtil.getStringWithKey(bundle, BUNDLE_TITLE);
        filePath = BundleUtil.getStringWithKey(bundle, BUNDLE_FILE_PATH);
        if (StringUtil.isEmpty(fileName)) {
            fileName = parseName(filePath);
        }

        filePath = BundleUtil.getStringWithKey(bundle, BUNDLE_FILE_PATH);
    }

    private void initViews() {
        ifBack = (IconFontTextView) this.findViewById(R.id.iv_top_left);
        tvTitle = (AlwaysMarqueeTextView) this.findViewById(R.id.tv_top_title);
        tvTopRight = (TextView) this.findViewById(R.id.tv_top_right);
        refreshTopRightView();

        tvTitle.setText(fileName);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.id_content, getDocPreviewFragment())
                .hide(getDocPreviewFragment())
                .commitAllowingStateLoss();

        RxView.clicks(ifBack)
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
                        finish();
                    }
                });

        RxView.clicks(tvTopRight)
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

                        downloadResFile();
                    }
                });

    }

    private void showDocPreviewFg() {
        tvTopRight.setVisibility(View.GONE);
        XXTHud.dismiss();

        docPreviewFragment = getDocPreviewFragment();

        if (null != docPreviewFragment && getSupportFragmentManager().getFragments().size() > 0) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .show(docPreviewFragment)
                    .commitAllowingStateLoss();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.id_content, docPreviewFragment)
                    .commitAllowingStateLoss();
        }
        docPreviewFragment.refreshFileDisplay(getFileLocalPath());
    }

    private void refreshTopRightView() {
        tvTopRight.setVisibility(View.GONE);
        if (isNetWorkFile(filePath)) {
            tvTopRight.setVisibility(View.VISIBLE);
        }
    }

    private boolean isNetWorkFile(String filePath) {
        boolean flag = false;

        if (!StringUtil.isEmpty(filePath) && filePath.startsWith("http")) {
            flag = true;
        }

        return flag;
    }

    private DocPreviewFragment getDocPreviewFragment() {
        if (null == docPreviewFragment) {
            docPreviewFragment = new DocPreviewFragment();
            docPreviewFragment.initData(filePath);
        }
        return docPreviewFragment;
    }

    /**
     * 下载资源文件
     */
    public void downloadResFile() {
        MultiThreadDownload multiThreadDownload = new MultiThreadDownload(context, filePath,
                getFileCacheDir(), getFileCacheName(), new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case DownloadFileUtil.START_DOWNLOAD_MSG:
                        XXTHud.show(context, "下载中...", true);
                        break;
                    case DownloadFileUtil.DOWNLOAD_SUCCESS_MSG:
                        XXTHud.dismiss();
                        showDocPreviewFg();
                        break;
                    case DownloadFileUtil.DOWNLOAD_FAIL_MSG:
                        tvTopRight.setVisibility(View.VISIBLE);
                        XXTHud.dismiss();
                        ToastUtil.displayToastLong(context, "下载失败");
                        break;
                    default:
                        break;
                }
            }
        });
        multiThreadDownload.start();
    }

    private String parseName(String url) {
        String fileName = null;
        try {
            fileName = url.substring(url.lastIndexOf("/") + 1);
        } finally {
            if (TextUtils.isEmpty(fileName)) {
                fileName = String.valueOf(System.currentTimeMillis());
            }
        }
        return fileName;
    }

    private String getFileCacheDir() {
        String dirPath = StringUtil.connectStrings(FileUtil.getLocalDir(), FILE_CACHE_DIR,
                fileName,"_",Md5Util.getMD5StringBit16(filePath)+"/");
        FileUtil.makeDir(dirPath);
        return dirPath;
    }

    private String getFileCacheName() {
        return fileName;
    }

    private String getFileLocalPath() {
        if (!StringUtil.isEmpty(filePath)) {
            if (filePath.startsWith("/") || filePath.startsWith("file:/")) {
                return filePath;
            } else {
                return StringUtil.connectStrings(getFileCacheDir(), getFileCacheName());
            }
        } else {
            return filePath;
        }
    }

    private void checkPermissions() {
        new RxPermissions(this)
                .requestEach(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .subscribe(new Action1<Permission>() {
                    @Override
                    public void call(Permission permission) {
                        if(permission.granted){
                            // 获得授权
                        } else if (permission.shouldShowRequestPermissionRationale){
                            //用户拒绝
                            //未获得授权
                            Toast.makeText(context, "您没有授权文件读写权限，文件将无法打开", Toast.LENGTH_SHORT).show();
                        } else {
                            //拒绝，且不再提示
                            //需要客户端给提示

                            displayPermissionTip();
                        }
                    }
                });
    }

    /**
     * 用户拒绝权限后的提示
     */
    private void displayPermissionTip() {
        String tip = "在设置中开启文件存储读写权限，否则无法打开文件";
        XXTDialog dialog = new XXTDialog(getApplicationContext());
        dialog.setDialogTitle("提示");
        dialog.setTip(tip);
        dialog.setBtnConfirm("去设置", View.VISIBLE);
        dialog.setBtnCancel("取消", View.VISIBLE);
        dialog.setOnXxtDialogBtnClickListener(new XXTDialog.OnXxtDialogBtnClickListener() {
            @Override
            public void onBtnConfirmClick() {
                Intent intent =  new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
            }

            @Override
            public void onBtnCancelClick() {
            }
        });

        dialog.setCanceledOnTouchOutside(false);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });
        dialog.show();
    }
}
