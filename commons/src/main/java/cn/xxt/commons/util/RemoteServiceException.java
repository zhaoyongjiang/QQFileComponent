package cn.xxt.commons.util;

import java.io.IOException;

/**
 * 请求远程服务器接口返回的异常
 * Created by zhanghuan on 16/4/20.
 */
public class RemoteServiceException  extends IOException {

    /** 异常码 */
    private int status;
    /** 异常说明 */
    private String message;
    /** 业务码 */
    private int code;

    public RemoteServiceException(int status) {
        this.status = status;
    }

    public RemoteServiceException(String detailMessage, int status) {
        super(detailMessage);
        this.status = status;
        this.message = detailMessage;
    }

    public RemoteServiceException(String detailMessage, int status, int code) {
        super(detailMessage);
        this.status = status;
        this.message = detailMessage;
        this.code = code;
    }

    public int getStatus() {
        return status;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
