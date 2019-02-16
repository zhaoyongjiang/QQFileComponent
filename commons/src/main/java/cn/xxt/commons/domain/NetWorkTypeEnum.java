package cn.xxt.commons.domain;

/**
 * Created by xxt-hxn on 16/3/14.
 */
public enum NetWorkTypeEnum {
    //未知网络类型
    NETWORK_TYPE_UNKNOWN(0,"UNKNOWN", "UNKNOWN"),
    //GPRS-2G网络
    NETWORK_TYPE_GPRS(1,"GPRS","2G"),
    //EDGE-2G网络
    NETWORK_TYPE_EDGE(2,"EDGE","2G"),
    //UMTS-3G网络
    NETWORK_TYPE_UMTS(3,"UMTS","3G"),
    //CDMA-2G网络
    NETWORK_TYPE_CDMA(4,"CDMA","2G"),
    //EVDO_0-3G网络
    NETWORK_TYPE_EVDO_0(5,"EVDO_0","3G"),
    //EVDO_A-3G网络
    NETWORK_TYPE_EVDO_A(6,"EVDO_A","3G"),
    //1xRTT-2G网络
    NETWORK_TYPE_1xRTT(7,"1xRTT","2G"),
    //HSDPA-3G网络
    NETWORK_TYPE_HSDPA(8,"HSDPA","3G"),
    //HSUPA-3G网络
    NETWORK_TYPE_HSUPA(9,"HSUPA","3G"),
    //HSPA-3G网络
    NETWORK_TYPE_HSPA(10,"HSPA","3G"),
    //IDEN-2G网络
    NETWORK_TYPE_IDEN(11,"IDEN","2G"),
    //EVDO_B-3G网络
    NETWORK_TYPE_EVDO_B(12,"EVDO_B","3G"),
    //LTE-4G网络
    NETWORK_TYPE_LTE(13,"LTE","4G"),
    //EHRPD-3G网络
    NETWORK_TYPE_EHRPD(14,"EHRPD","3G"),
    //HSPAP-3G网络
    NETWORK_TYPE_HSPAP(15,"HSPAP","3G");

    private int id;
    private String name;
    private String generationName;

    private NetWorkTypeEnum(int id, String name, String generationName) {
        this.id = id;
        this.name = name;
        this.generationName = generationName;
    }

    public static NetWorkTypeEnum getNetWork(int id) {
        for (NetWorkTypeEnum n : NetWorkTypeEnum.values()) {
            if (n.id == id) {
                return n;
            }
        }
        return NETWORK_TYPE_UNKNOWN;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGenerationName() {
        return generationName;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
