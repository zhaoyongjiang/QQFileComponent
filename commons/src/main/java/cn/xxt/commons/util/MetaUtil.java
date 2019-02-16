package cn.xxt.commons.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/**
 * Created by Luke on 16/7/19.
 */
public class MetaUtil {

    public final static String HOST_ID = "HOST_ID";

    /**
     * 获取int类型的meta-data
     * @param context 上下文
     * @param name key
     * @return value
     */
    public static int getMetaIntValue(Context context, String name) {
        int value= -1;
        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            value = appInfo.metaData.getInt(name);
        } catch (PackageManager.NameNotFoundException e) {
        }
        return value;
    }
}
