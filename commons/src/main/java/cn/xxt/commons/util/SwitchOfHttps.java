package cn.xxt.commons.util;

import android.content.Context;

/**
 * @author dy
 * @date 2017/12/8
 * HTTPs 开关的控制类
 */
public class SwitchOfHttps {

    /** 测试环境https 的开关：true 使用http false 使用https */
    private static boolean closeTestHttps = false;

    /** 正式环境https 的开关：true 使用http false 使用https */
    private static boolean closeHttps = false;

    public static boolean isCloseTestHttps() {
        return closeTestHttps;
    }

    public static void setCloseTestHttps(Context context, boolean closeTestHttps) {
        SwitchOfHttps.closeTestHttps = closeTestHttps;
    }

    public static void loadTestSwitchOfHttps(Context context) {
    }

    public static boolean isCloseHttps() {
        return closeHttps;
    }

    public static void setCloseHttps(boolean closeHttps) {
        SwitchOfHttps.closeHttps = closeHttps;
    }
}
