package cn.xxt.commons.util;

import java.io.Serializable;
import java.util.Map;

/**
 * 序列化map供Bundle传递map使用
 *
 * Created by zyj on 16/12/14.
 */

public class SerializableMap implements Serializable {
    private Map<String, Object> map;

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }
}
