package cn.xxt.file.internal.domain;

/**
 * Created by zyj on 2017/8/15.
 */

public enum FileTypeEnum {
    //约定：
    // 将文件存入数据库用大分类（1，2，3，4，5）。。
    // 取出来后的文件类型，可按各子分类处理（11，12，13，14）。。
    // 也就是说，fileinfo模型中，存的是子分类


    TYPE_DOC(1),
    TYPE_DOC_WORD(11),
    TYPE_DOC_EXCEL(12),
    TYPE_DOC_PDF(13),
    TYPE_DOC_PPT(14),
    TYPE_AUDIO(2),
    TYPE_IMAGE(3),
    TYPE_VIDEO(4),
    TYPE_OTHER(5),
    TYPE_OTHER_TXT(51),
    TYPE_OTHER_ZIP(52),
    TYPE_OTHER_RAR(53),
    TYPE_OTHER_APK(54),
    TYPE_ALL(6);

    FileTypeEnum(int fileType) {
        this.fileType = fileType;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    private int fileType;
}
