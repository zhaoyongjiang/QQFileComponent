package cn.xxt.file.internal.domain;

/**
 * Created by zyj on 2017/8/9.
 */

public enum FileDownloadStatusEnum {

    FILE_DOWNLOAD_STATUS_READY(0),
    FILE_DOWNLOAD_STATUS_NOT_YET(1),
    FILE_DOWNLOAD_STATUS_DOWNLOADING(2),
    FILE_DOWNLOAD_STATUS_DOWNLOADED(3);

    private int fileDownloadStatus;

    public int getFileDownloadStatus() {
        return fileDownloadStatus;
    }

    public void setFileDownloadStatus(int fileDownloadStatus) {
        this.fileDownloadStatus = fileDownloadStatus;
    }

    FileDownloadStatusEnum(int fileDownloadStatus) {
        this.fileDownloadStatus = fileDownloadStatus;
    }
}
