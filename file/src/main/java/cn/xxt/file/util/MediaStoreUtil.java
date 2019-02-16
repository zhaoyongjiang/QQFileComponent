package cn.xxt.file.util;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import cn.xxt.commons.util.StringUtil;

/**
 * Created by zyj on 2017/9/21.
 */

public class MediaStoreUtil {

    public interface MediaStoreUtilInterface {
        void syncComplete();
    }

    public static void setMediaStoreUtilInterface(MediaStoreUtilInterface mediaStoreUtilInterface) {
        MediaStoreUtil.mediaStoreUtilInterface = mediaStoreUtilInterface;
    }

    private static MediaStoreUtilInterface mediaStoreUtilInterface;

    /**
     * 是4.4以下的版本走的是else ，需要用到的是文件夹的目录..4.4以上用的是文件绝对路径
     * @param context
     * @param path
     */
    public static void updateMediaStore(final  Context context, final String path) {
        //版本号的判断  4.4为分水岭，发送广播更新媒体库
        if (!StringUtil.isEmpty(path)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            {
                highVersionScan(context, new String[]{path});
            } else {
                lowVersionScan(context, path);
            }
        }
    }

    public static void updateMediaStore(final  Context context, final List<String> pathList) {
        if (pathList != null && pathList.size() > 0) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            {
                String[] paths = (String[])pathList.toArray(new String[pathList.size()]);
                highVersionScan(context, paths);
            } else {
                Iterator iterator = pathList.iterator();
                while (iterator.hasNext()) {
                    String path = (String)iterator.next();
                    lowVersionScan(context, path);
                }

                //4.4一下的手机走这里的流程。循环同步完，回调重新获取数据
                if (mediaStoreUtilInterface != null) {
                    mediaStoreUtilInterface.syncComplete();
                }
            }
        }
    }

    private static void highVersionScan(final  Context context, final String[] paths) {
        final int[] i = {0};
        MediaScannerConnection.scanFile(context, paths, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(uri);
                context.sendBroadcast(mediaScanIntent);

                i[0]++;

                //这里同步完成后，回调再次去获取数据
                if (i[0] == paths.length) {
                    if (mediaStoreUtilInterface != null) {
                        mediaStoreUtilInterface.syncComplete();
                    }
                }
            }
        });
    }

    private static void lowVersionScan(final  Context context, final String path) {
        File file = new File(path);
        String relationDir = file.getParent();
        File file1 = new File(relationDir);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(file1.getAbsoluteFile())));
    }
}
