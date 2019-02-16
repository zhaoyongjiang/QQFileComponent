package cn.xxt.commons.util;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 界面bundle传值工具类
 *
 * Created by zyj on 16/12/27.
 */

public class BundleUtil {
    /**
     * 获取string类型value
     * @param bundle
     * @param key
     * @return
     */
    public static String getStringWithKey(Bundle bundle, String key) {
        String value = "";
        if (bundle != null
                && bundle.containsKey(key)
                && bundle.get(key) != null) {
            value = bundle.getString(key);
        }
        return value;
    }

    /**
     * 获取int类型value
     * @param bundle
     * @param key
     * @return
     */
    public static int getIntegerWithKey(Bundle bundle, String key) {
        int value = -1;
        if (bundle != null
                && bundle.containsKey(key)
                && bundle.get(key) != null) {
            value = bundle.getInt(key);
        }
        return value;
    }

    /**
     * 获取long类型的value
     * @param bundle
     * @param key
     * @return
     */
    public static long getLongWithKey(Bundle bundle, String key) {
        long value = -1;
        if (bundle != null
                && bundle.containsKey(key)
                && bundle.get(key) != null) {
            value = bundle.getLong(key);
        }
        return value;
    }

    /**
     * 获取double类型value
     * @param bundle
     * @param key
     * @return
     */
    public static double getDoubleWithKey(Bundle bundle, String key) {
        double value = -1;
        if (bundle != null
                && bundle.containsKey(key)
                && bundle.get(key) != null) {
            value = bundle.getDouble(key);
        }
        return value;
    }

    /**
     * 获取float类型的value
     * @param bundle
     * @param key
     * @return
     */
    public static float getFloatWithKey(Bundle bundle, String key) {
        float value = -1;
        if (bundle != null
                && bundle.containsKey(key)
                && bundle.get(key) != null) {
            value = bundle.getFloat(key);
        }
        return value;
    }

    /**
     * 获取布尔类型的value
     * @param bundle
     * @param key
     * @return
     */
    public static boolean getBooleanWithKey(Bundle bundle, String key) {
        boolean value = false;
        if (bundle != null
                && bundle.containsKey(key)
                && bundle.get(key) != null) {
            value = bundle.getBoolean(key);
        }
        return value;
    }

    /**
     * 用法:
     * 传:
     * Intent intent = new Intent();
     * intent.setClass(this, UnitStuOperateActivity.class);
     * SerializableMap stuInfoMap = new SerializableMap();
     * stuInfoMap.setMap(stuInfo);
     * Bundle bundle = new Bundle();
     * bundle.putSerializable(KEY_STUINFO, stuInfoMap);
     * intent.putExtras(bundle);
     *
     * 取:
     * 用该方法获得到SerializableMap ,然后serializableMap.getMap();获得传递的map
     *
     * 获取序列化类型的value:在bundle中传递map。可以用
     * @param bundle
     * @param key
     * @return
     */
    public static SerializableMap getSerializableWithKey(Bundle bundle, String key) {
        SerializableMap value = new SerializableMap();
        if (bundle != null
                && bundle.containsKey(key)
                && bundle.get(key) != null) {
            value = (SerializableMap) bundle.get(key);
        }
        return value;
    }

    /**
     * 用法:
     * 参考上面那个方法的用法
     *
     * 获取map类型的value
     * @param bundle
     * @param key
     * @return
     */
    public static Map<String, Object> getMapWithKey(Bundle bundle, String key) {
        Map<String, Object> value = new HashMap<>();
        if (bundle != null
                && bundle.containsKey(key)
                && bundle.get(key) != null) {
            SerializableMap serializableMap = (SerializableMap) bundle.get(key);
            value = serializableMap != null ? serializableMap.getMap() : null;
        }
        return value;
    }

    /**
     * 用法:
     * 传:
     * List<Map<String, Object>> memberList = new ArrayList<>();
     * Bundle bundle = new Bundle();
     * ArrayList list = new ArrayList<>();
     * list.add(memberList);
     * bundle.putInt(AllChatMemberActivity.BUNDLE_GROUPTYPE_KEY, groupType);
     * bundle.putParcelableArrayList(AllChatMemberActivity.BUNDLE_MEMBERLIST_KEY, list);
     *
     * 取:用到该方法
     *
     *
     * 获取List<Map<String, Object>>类型的value
     * @param bundle
     * @param key
     * @return
     */
    public static List<Map<String, Object>> getMapListWithKey(Bundle bundle, String key) {
        List<Map<String, Object>> value = new ArrayList<>();
        if (bundle != null
                && bundle.containsKey(key)
                && bundle.get(key) != null) {
            ArrayList list = bundle.getParcelableArrayList(key);
            value = (List<Map<String, Object>>) list.get(0);
        }
        return value;
    }

    /**
     * 获取字符串数组类型的value
     * @param bundle
     * @param key
     * @return
     */
    public static String[] getStringArrWithKey(Bundle bundle, String key) {
        String[] strings = new String[]{};
        if (bundle != null
                && bundle.containsKey(key)
                && bundle.get(key) != null) {
            strings = bundle.getStringArray(key);
        }
        return strings;
    }

    /**
     * 获取string列表类型的value
     * @param bundle
     * @param key
     * @return
     */
    public static List<String> getStringArrListWithKey(Bundle bundle, String key) {
        List<String> list = new ArrayList<>();
        if (bundle != null
                && bundle.containsKey(key)
                && bundle.get(key) != null) {
            list = bundle.getStringArrayList(key);
        }
        return list;
    }
}
