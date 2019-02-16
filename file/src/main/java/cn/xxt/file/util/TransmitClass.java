package cn.xxt.file.util;

import java.util.HashMap;
import java.util.Map;

import cn.xxt.file.internal.domain.FileInfo;

/**
 * Created by zyj on 2017/11/18.
 */

public class TransmitClass {
    public static FileInfo fileInfo;

    public static Map<Long, FileDownloadManager> fileDownloadManagerMap = new HashMap<>();
}
