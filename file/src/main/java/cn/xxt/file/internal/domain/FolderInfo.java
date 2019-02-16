package cn.xxt.file.internal.domain;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * 该类主要是图片文件夹模型
 *
 *
 *
 * Created by zyj on 2017/8/9.
 */

public class FolderInfo {
    /** 文件夹名 */
    public String name;

    /** 文件夹路径 */
    public String path;

    /** 文件夹中图片 */
    public List<FileInfo> fileInfos = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<FileInfo> getFileInfos() {
        return fileInfos;
    }

    public void setFileInfos(List<FileInfo> fileInfos) {
        this.fileInfos = fileInfos;
    }

}
