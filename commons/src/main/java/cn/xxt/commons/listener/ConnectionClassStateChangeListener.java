package cn.xxt.commons.listener;

import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;

/**
 * 类作用说明
 *
 * @author wangxiuli
 * @date 2018/5/2
 */
public interface ConnectionClassStateChangeListener extends ConnectionClassManager.ConnectionClassStateChangeListener {

    /**
     * 网络变化时方法
     *
     * @param bandwidthState
     */
    @Override
    void onBandwidthStateChange(ConnectionQuality bandwidthState);

}
