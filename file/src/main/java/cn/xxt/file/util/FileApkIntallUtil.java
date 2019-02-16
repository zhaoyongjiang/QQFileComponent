package cn.xxt.file.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import java.io.File;

/**
 * Created by zyj on 2017/11/2.
 */

public class FileApkIntallUtil {

    /**返回安装apk的Intent*/
    public static Intent getFileIntent(Context mContext,String fileSavePath) {
        File apkfile = new File(fileSavePath);
        if (!apkfile.exists()) {
            return null;
        }
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);

        Uri uri;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String authority = mContext.getApplicationInfo().packageName + ".provider";
            uri = FileProvider.getUriForFile(mContext.getApplicationContext(), authority, apkfile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//添加这一句表示对目标应用临时授权该Uri所代表的文件【很重要】
        } else {
            uri = Uri.fromFile(apkfile);
        }
        intent.setDataAndType(uri, FileUtil.getMIMEType(apkfile));
        return intent;
    }

    /**
     * 安装apk【如果项目中需要使用这个方法的话，需要申请运行时权限（读写文件的权限）、需要特出处理Android8.0的请求未知来源权限】
     */
    public static void intallApk(Context mContext,String filePath) {
        Intent intent = getFileIntent(mContext,filePath);
        if(intent != null){
            mContext.startActivity(intent);
        }
    }
}
