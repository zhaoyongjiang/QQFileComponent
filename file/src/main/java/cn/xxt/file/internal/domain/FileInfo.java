package cn.xxt.file.internal.domain;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.io.Serializable;

/**
 * Created by zyj on 2017/8/9.
 */

public class FileInfo implements MultiItemEntity, Serializable{

    /** 是否是路径 */
    public boolean isDirectory;

    /** 是否被选中 */
    public boolean isCheck = false;

    /** 是否是九宫格图片 */
    public boolean isGridPhoto = true;



    /******* 文件信息 *******/
    /** 文件id */
    public long fileId;

    /** 文件名 */
    public String fileName;

    /** 文件大小 */
    public long fileSize;

    /** 音频时长 */
    public long voiceDuration = 0;

    /** 文件后缀 */
    public String fileSuffix = "";

    /** 文件类型 */
    public int fileType;

    /** 文件服务器链接 */
    public String fileServerPath;

    /** 文件路径 */
    public String fileLocalPath;

    /** 文件下载状态 */
    public int downloadStatus = 0;

    /** 文件收发类型：1：发送  2：接收 */
    public int sendType;

    /** 接收方名称：如：发送会话组的名称 */
    public String destName;

    /** 发送功能：区分是哪个功能接收/发送的文件 */
    public int sendSource;

    /** 文件创建日期 */
    public long createDate;

    /** 更新时间 */
    public long updateDate;

    /** 状态：0正常，1删除，2失效 */
    public int status;

//    /** 文件来源：1：本机文件  2：服务器文件 */
//    public int fileSource;

    public FileInfo() {

    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public boolean isGridPhoto() {
        return isGridPhoto;
    }

    public void setGridPhoto(boolean gridPhoto) {
        isGridPhoto = gridPhoto;
    }

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getVoiceDuration() {
        return voiceDuration;
    }

    public void setVoiceDuration(long voiceDuration) {
        this.voiceDuration = voiceDuration;
    }

    public String getFileSuffix() {
        return fileSuffix;
    }

    public void setFileSuffix(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public String getFileServerPath() {
        return fileServerPath;
    }

    public void setFileServerPath(String fileServerPath) {
        this.fileServerPath = fileServerPath;
    }

    public String getFileLocalPath() {
        return fileLocalPath;
    }

    public void setFileLocalPath(String fileLocalPath) {
        this.fileLocalPath = fileLocalPath;
    }

    public int getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(int downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public int getSendType() {
        return sendType;
    }

    public void setSendType(int sendType) {
        this.sendType = sendType;
    }

    public String getDestName() {
        return destName;
    }

    public void setDestName(String destName) {
        this.destName = destName;
    }

    public int getSendSource() {
        return sendSource;
    }

    public void setSendSource(int sendSource) {
        this.sendSource = sendSource;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//
//    }

//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeByte(this.isDirectory ? (byte) 1 : (byte) 0);
//        dest.writeByte(this.isCheck ? (byte) 1 : (byte) 0);
//        dest.writeByte(this.isGridPhoto ? (byte) 1 : (byte) 0);
//        dest.writeLong(this.fileId);
//        dest.writeString(this.fileName);
//        dest.writeLong(this.fileSize);
//        dest.writeLong(this.voiceDuration);
//        dest.writeString(this.fileSuffix);
//        dest.writeInt(this.fileType);
//        dest.writeString(this.fileServerPath);
//        dest.writeString(this.fileLocalPath);
//        dest.writeInt(this.downloadStatus);
//        dest.writeInt(this.sendType);
//        dest.writeString(this.destName);
//        dest.writeString(this.sendSource);
//        dest.writeLong(this.createDate);
//        dest.writeLong(this.updateDate);
//        dest.writeInt(this.status);
//    }

    @Override
    public int getItemType() {
        if (this.fileType == FileTypeEnum.TYPE_IMAGE.getFileType()) {
            if (isGridPhoto) {
                return ItemViewTypeEnum.ITEM_VIEW_TYPE_PHOTO.getItemViewType();
            }
        }
        return ItemViewTypeEnum.ITEM_VIEW_TYPE_FILE.getItemViewType();
    }

//    protected FileInfo(Parcel in) {
//        this.isDirectory = in.readByte() != 0;
//        this.isCheck = in.readByte() != 0;
//        this.isGridPhoto = in.readByte() != 0;
//        this.fileId = in.readLong();
//        this.fileName = in.readString();
//        this.fileSize = in.readLong();
//        this.voiceDuration = in.readLong();
//        this.fileSuffix = in.readString();
//        this.fileType = in.readInt();
//        this.fileServerPath = in.readString();
//        this.fileLocalPath = in.readString();
//        this.downloadStatus = in.readInt();
//        this.sendType = in.readInt();
//        this.destName = in.readString();
//        this.sendSource = in.readString();
//        this.createDate = in.readLong();
//        this.updateDate = in.readLong();
//        this.status = in.readInt();
//    }

//    public static final Creator<FileInfo> CREATOR = new Creator<FileInfo>() {
//        @Override
//        public FileInfo createFromParcel(Parcel source) {
//            return null;
//        }
//
//        @Override
//        public FileInfo[] newArray(int size) {
//            return new FileInfo[size];
//        }
//    };


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        FileInfo fileInfo = (FileInfo) obj;

        if (fileId != fileInfo.getFileId()) {
            return false;
        }
        return fileId == fileInfo.getFileId();

    }
}
