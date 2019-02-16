package cn.xxt.file.util;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import cn.xxt.commons.util.StringUtil;
import cn.xxt.commons.util.download.DownloadFileUtil;
import cn.xxt.commons.util.download.MultiThreadDownload;
import cn.xxt.file.internal.data.local.FileDb;


/**
 * Created by zyj on 2017/9/1.
 */

public class FileDownloadManager{
    /** 打印：tag */
    public static final String TAG = FileDownloadManager.class.getSimpleName();

    private long fileId = 0;

    private String dir = FileUtil.getFileSaveDir();

    private String fileName = "";

    private String url;

    private OnFileDownloadListener onCallBackListener;

    private Context context;

    public FileDownloadManager(long fileId, String dir, String fileName, String url, Context context) {
        if (dir != null && dir.length() > 0) {
            this.dir = dir;
        }

        this.fileId = fileId;

        this.fileName = FileUtil.getFileSaveName(fileName, url);

        this.url = url;
        this.context = context;
    }

    /**
     * 内部接口
     */
    public interface OnFileDownloadListener {
        /**
         * 开始下载
         */
        void onDownloadBegin(long fileId, String fileLocalPath);

        /**
         * 下载进度
         * @param percent 百分比
         */
        void onDownloadProgress(int percent);

        /**
         * 暂停下载
         */
        void onDownloadPause(int percent);

        /**
         * 下载完成
         */
        void onDownloadComplete(String url, long fileId, String localPath, String fileSaveName);

        /**
         * 下载失败
         */
        void onDownloadError(String url);
    }

    public void downloadFile(final OnFileDownloadListener onFileDownloadListener){
        if (StringUtil.isEmpty(url)) {
            onFileDownloadListener.onDownloadError(url);
            return;
        }

        this.onCallBackListener = onFileDownloadListener;

        onFileDownloadListener.onDownloadBegin(fileId, getFileSavePath());

        Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == DownloadFileUtil.DOWNLOAD_SUCCESS_MSG) {
                    //完成
                    onCallBackListener.onDownloadComplete(url, fileId, getFileSavePath(), fileName);

                    //同步多媒体数据库,不然刚下载完成的文件，在本机中找不到
                    MediaStoreUtil.updateMediaStore(context, getFileSavePath());
                } else if (msg.what == DownloadFileUtil.DOWNLOAD_FAIL_MSG) {
                    //失败
                    onCallBackListener.onDownloadError(url);

                    //下载失败，删除文件
                    DownloadFileUtil.deleteFile(getFileSavePath());

                    //删除数据库记录
                    deleteFileDataByFileId(fileId);
                } else if (msg.what == DownloadFileUtil.UPDATE_DOWNLOAD_MSG) {
                    //进度
                    Bundle bundle = msg.getData();
                    int percent = bundle.getInt("percent");
                    onCallBackListener.onDownloadProgress(percent);
                }
                return false;
            }
        });

        MultiThreadDownload multiThreadDownload = new MultiThreadDownload(context, url, dir,
                fileName, handler);
        multiThreadDownload.start();
    }

    /**
     * 更新回调监听对象
     * @param onFileDownloadListener
     */
    public void updateDownLoadListener(OnFileDownloadListener onFileDownloadListener) {
        this.onCallBackListener = onFileDownloadListener;
    }

    public boolean deleteFileDataByFileId(long fileId) {
        boolean result = false;

        result = FileDb.getInstance(context).deleteFileByFileId(0, fileId);

        return result;
    }

    public String getFileSavePath() {
        String filePath;

        filePath = StringUtil.connectStrings(dir, fileName);

        return filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileId() {
        return fileId;
    }
}
