package cn.xxt.commons.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import cn.xxt.commons.domain.NetWorkTypeEnum;
import retrofit2.adapter.rxjava.HttpException;

import static android.app.ApplicationErrorReport.TYPE_NONE;


public class NetworkUtil {

    public static final String NETWORK_WIFI = "Wifi";

    /**
     * Returns true if the Throwable is an instance of RetrofitError with an
     * http status code equals to the given one.
     */
    public static boolean isHttpStatusCode(Throwable throwable, int statusCode) {
        return throwable instanceof HttpException
                && ((HttpException) throwable).code() == statusCode;
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    /**
     * 获取网络类型
     * @param context 上下文
     * @return 网络类型
     */
    public static String getNetworkType(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        String netInfo = "";
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                netInfo = NETWORK_WIFI;
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                int subType = info.getSubtype();
                netInfo = NetWorkTypeEnum.getNetWork(subType).getGenerationName();
            }
        }
        return netInfo;
    }

    /**
     * 获取网络连接类型
     * @param context
     * @return
     */
    public static int getNetwordType(Context context) {
        int netType = TYPE_NONE;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null) {
            //飞行模式，为null
            netType = info.getType();
        }
        return netType;
    }

    /**
     * 获取wifi名称，只有在wifi网络连接的情况下才能获取
     * @param context 上下文
     * @return wifi网络名称
     */
    public static String getWifiName(Context context) {
        String wifiName = "";
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            wifiName = wifiInfo.getSSID();
        }
        return wifiName;
    }

    /**
     * 获取BSSID：只有在wifi网络连接的情况下才能获取
     * @param context 上下文
     * @return wifi热点
     */
    public static String getBssid(Context context) {
        String bssid = "";
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            bssid = wifiInfo.getBSSID();
        }
        return bssid;
    }

    /**
     * 获取运营商信息
     * @param context 上下文
     * @return 运营商
     */
    public static String getNetOperator(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String operatorString = telephonyManager.getSimOperator();
        if (operatorString == null) {
            return "UNKNOWN";
        } if (operatorString.equals("46000") || operatorString.equals("46002")) {
            return "中国移动";
        } else if(operatorString.equals("46001")) {
            //中国联通
            return "中国联通";
        } else if(operatorString.equals("46003")) {
            //中国电信
            return "中国电信";
        }
        //error
        return "UNKNOWN";
    }

    /**
     * 获取网速：只在wifi状态下获取
     * @param context 上下文
     * @return 网速（wifi网络）
     */
    public static int getLinkSpeed(Context context) {
        int linkSpeed = 0;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            linkSpeed = wifiInfo.getLinkSpeed();
        }
        return linkSpeed;
    }

    /**
     * 获取网络信号的强度：在wifi状态下获取
     * @param context 上下文
     * @return 网络信号强度：0到-50表示信号最好，-50到-70表示信号偏差，小于-70表示最差，有可能连接不上或者掉线
     */
    public static int getNetSingal(Context context) {
        int netSingal = 0;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            netSingal = wifiInfo.getRssi();
        }
        return netSingal;
    }

    /**
     * 检测当前的网络连接是否可用
     * @param context
     * @return true 可用 false 不可用
     */
    public static boolean checkNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null)
            {
                // 当前网络是连接的
                return info.isAvailable();
            }
        }
        return false;
    }
}