package cn.xxt.commons.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Map;


/**
 * json转map
 */
public class JsonToMapUtil {
    private static final Gson GSON = new Gson();

    /**
     * 将json字符串转为Map对象
     *
     * @param jsonString json字符串
     * @return Map对象
     */
    public static Map<String, Object> toMap(String jsonString) {
        Map<String, Object> map = null;

        try {
            if (null == jsonString || "".equals(jsonString)
                    || !(jsonString.startsWith("{") && jsonString.endsWith("}"))) {
                return null;
            }

            map = GSON.fromJson(jsonString, Map.class);
        } catch (Exception e) {

        }

        return map;
    }

    /**
     * 将{@link JsonObject}对象转为Map对象
     * @param json {@link JsonObject}对象
     * @return Map对象
     */
    public static Map<String, Object> toMap(JsonObject json) {
        Map<String, Object> map = null;

        try {
            map = GSON.fromJson(json, Map.class);
        } catch (Exception e) {

        }

        return map;
    }
}