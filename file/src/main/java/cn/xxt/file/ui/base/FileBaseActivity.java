package cn.xxt.file.ui.base;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.tbruyelle.rxpermissions.Permission;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.ArrayList;
import java.util.List;

import cn.xxt.commons.injection.base.BaseApplication;
import cn.xxt.commons.widget.XXTDialog;
import cn.xxt.file.injection.component.DaggerFileActivityComponent;
import cn.xxt.file.injection.component.DaggerFileApplicationComponent;
import cn.xxt.file.injection.component.FileActivityComponent;
import cn.xxt.file.injection.component.FileApplicationComponent;
import cn.xxt.file.injection.module.FileActivityModule;
import cn.xxt.file.injection.module.FileApplicationModule;
import cn.xxt.file.internal.domain.FileInfo;
import cn.xxt.library.ui.base.BaseActivity;
import rx.functions.Action1;

/**
 * Created by zyj on 2017/8/16.
 */

public class FileBaseActivity extends BaseActivity {
    //adapter -》activity 发送文件通知。字段
    public static final String FILE_RXBUS_TAG = "FILE_RXBUS_TAG";
    public static final String KEY_FILE_INFO = "KEY_FILE_INFO";

    public List<FileInfo> selectedFileInfoList = new ArrayList<>();

    /** 标杆qq，默认最多选择20个文件 */
    public static int selectFileMaxNum = 1;
    public static double singleFileMaxSize = Math.pow(1024, 2) * 8;

    /** 用户身份标识 */
    public int webId = 0;

    private FileActivityComponent fileActivityComponent;
    FileApplicationComponent fileApplicationComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPermissions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public FileActivityComponent getActivityComponent() {
        if (fileActivityComponent == null) {
            fileActivityComponent = DaggerFileActivityComponent.builder()
                    .fileActivityModule(new FileActivityModule(this))
                    .fileApplicationComponent(getComponent())
                    .build();
        }
        return fileActivityComponent;
    }

    public FileApplicationComponent getComponent() {
        if (fileApplicationComponent == null) {
            fileApplicationComponent = DaggerFileApplicationComponent.builder()
                    .fileApplicationModule(new FileApplicationModule(BaseApplication.getInstance()))
                    .build();
        }
        return fileApplicationComponent;
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
                            Log.i("", "call: 获得授权");
                        } else if (permission.shouldShowRequestPermissionRationale){
                            //用户拒绝
                            //未获得授权
                            finish();
                            Toast.makeText(FileBaseActivity.this, "您没有授权该权限，请在设置中打开授权", Toast.LENGTH_SHORT).show();
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
        String tip = "在设置中开启文件存储读写权限";
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
