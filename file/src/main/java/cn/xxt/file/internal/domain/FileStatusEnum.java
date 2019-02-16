package cn.xxt.file.internal.domain;

/**
 * Created by zyj on 2017/8/23.
 */

public enum FileStatusEnum {
    FILE_STATUS_NORMAL(0),
    FILE_STATUS_DELETE(1),
    FILE_STATUS_UNVAILIBLE(2);

    private int fileStatus;

    public int getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(int fileStatus) {
        this.fileStatus = fileStatus;
    }

    FileStatusEnum(int fileStatus) {
        this.fileStatus = fileStatus;
    }
}
