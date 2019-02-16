package cn.xxt.file.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.xxt.commons.util.ToastUtil;
import cn.xxt.file.internal.data.local.FileDb;
import cn.xxt.file.internal.domain.FileInfo;
import cn.xxt.file.ui.manager.FileManagerMainActivity;
import cn.xxt.file.ui.manager.FileOpenActivity;
import cn.xxt.file.ui.selector.FileSelectorMainActivity;
import cn.xxt.file.util.ObjectSpUtil;

import static cn.xxt.file.api.FileComponentCommonValue.KEY_WEBID;
import static cn.xxt.file.ui.manager.FileOpenActivity.BUNDLE_FILEINFO;

/**
 * Created by zyj on 2017/12/18.
 */

public class FileComponent{

    private static String fileName = "file_commons_sp_file_name";
    private static String file_key = "file_commons_sp_config_map";
    private static String filetype_key = "filetype_key";

    public enum SizeUnitEnum {
        //kb
        KB_TYPE,
        //MB
        MB_TYPE,
        //GB
        GB_TYPE
    }

    /**
     * 保存文件：外部调用，穿件fileinfo模型，传进来，组件内保存到filedb中，返回组件内的文件标识：fileid，调用者拿到fileid需要自行维护维护。
     *
     * 例：收、发一个文件，需要调用saveFileInfo，组件内作为最近文件数据保存起来，返回给调用者fileid。
     *
     * @param fileInfo
     * @return
     */
    public static long saveFileInfo(Context context, int webId, FileInfo fileInfo) {
        FileDb fileDb = FileDb.getInstance(context);

        long fileId = fileDb.insertFileInfo(webId, fileInfo);

        return fileId;
    }

    /**
     * 选择文件
     * @param context
     * @param webId
     * @param requestCode
     */
    public static void selectFile(Context context, int webId, int requestCode) {
        Activity activity = (Activity) context;
        Intent intent = new Intent();
        intent.setClass(context, FileSelectorMainActivity.class);

        Bundle bundle = new Bundle();
        bundle.putInt(KEY_WEBID, webId);

        intent.putExtras(bundle);

        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 删除文件
     * @param context
     * @param webId
     * @param fileIdList
     * @return
     */
    public static boolean deleteFile(Context context, int webId, List<Long> fileIdList) {
        boolean result = false;

        if (fileIdList == null || fileIdList.size() == 0) {
            return result;
        }

        FileDb fileDb = FileDb.getInstance(context);
        result = fileDb.deleteFileByFileIdList(webId, fileIdList);

        return result;
    }

    /**
     * 管理文件
     * @param context
     * @param webId
     */
    public static void manageFile(Context context, int webId) {
        Activity activity = (Activity) context;
        Intent intent = new Intent();
        intent.setClass(context, FileManagerMainActivity.class);

        Bundle bundle = new Bundle();
        bundle.putInt(KEY_WEBID, webId);

        intent.putExtras(bundle);

        activity.startActivity(intent);
    }

    /**
     * 打开文件：标杆qq，沟通里，可以直接使用这个方法开发所有文件。音乐，视频（非xxt视频），文档等。
     * 如果是web的话，请调用工程里的webviewpageactivity/videoactivity
     *
     * 注：也可以直接使用webview模块里的X5FileOpenUtil打开文件。。这个类的音频是使用的视频播放。
     * 所以，没有具体的播放界面。可以体验qq的音乐播放，有个播放界面。
     *
     * @param context
     * @param webId
     */
    public static void openFile(Context context, int webId, FileInfo fileInfo) {
        //TODO 业务工程端 点击打开未下载的文件，组件的下载回调要开发测试

        Activity activity = (Activity) context;
        Intent intent = new Intent();
        intent.setClass(context, FileOpenActivity.class);

        Bundle bundle = new Bundle();
        bundle.putInt(KEY_WEBID, webId);
        bundle.putSerializable(BUNDLE_FILEINFO, fileInfo);

        intent.putExtras(bundle);

        activity.startActivity(intent);
    }

//    https://www.jianshu.com/p/eec057ae3e00

    public static Builder builder(Context context) {

        return Builder.getInstance(context);
    }

    public static class Builder {

        private static Builder builder = null;

        private Builder() {

        }

        public static Builder getInstance(Context context) {
            if (builder == null) {
                synchronized (Builder.class) {
                    if (builder == null) {
                        builder = new Builder();
                    }
                }
            }
            builder.context = context;
            return builder;
        }

        private Context context;
        private int maxNum = 20;
        private double maxSize = 8*Math.pow(1024, 2);
        private int concorrentDownloadMaxNum = 3;

        private boolean showLocal = true;
        private boolean showRecent = true;

        private List<Integer> fileTypeList = new ArrayList<>();

        public int getMaxNum() {
            return maxNum;
        }

        public Builder setMaxNum(int maxNum) {
            this.maxNum = maxNum;
            return this;
        }

        public double getMaxSize() {
            return maxSize;
        }

        public Builder setMaxSize(int maxSize, FileComponent.SizeUnitEnum unitType) {
            double length  = (double) maxSize;

            if (unitType == SizeUnitEnum.KB_TYPE) {
                length *= Math.pow(1024, 1);
            } else if (unitType == SizeUnitEnum.MB_TYPE) {
                length *= Math.pow(1024, 2);
            } else if (unitType == SizeUnitEnum.GB_TYPE) {
                length *= Math.pow(1024, 3);
            }

            this.maxSize = length;

            return this;
        }

        public int getConcorrentDownloadMaxNum() {
            return concorrentDownloadMaxNum;
        }

        public Builder setConcorrentDownloadMaxNum(int concorrentDownloadMaxNum) {
            this.concorrentDownloadMaxNum = concorrentDownloadMaxNum;
            return this;
        }

        public boolean isShowLocal() {
            return showLocal;
        }

        public Builder setShowLocal(boolean showLocal) {
            this.showLocal = showLocal;
            return this;
        }

        public boolean isShowRecent() {
            return showRecent;
        }

        public Builder setShowRecent(boolean showRecent) {
            this.showRecent = showRecent;
            return this;
        }

        //FileTypeEnum 文件中有文件分类枚举。使用大类，不要使用小类。1，2，3，4，5，6
        public Builder setShowFileType(Integer... sub) {

            Object object = ObjectSpUtil.readObject(context, file_key, fileName);
            List<Integer> fileTypeList = new ArrayList<>();
            if (object != null) {
                Map<String, Object> map = (Map<String, Object>)object;
                map.remove(filetype_key);
            }

            if (sub.length == 0) {
                ToastUtil.displayToastLong(context, "没有配置文件类型");
            } else {
                for (Integer integer : sub) {
                    fileTypeList.add(integer);
                }
            }

            this.fileTypeList.clear();
            this.fileTypeList.addAll(fileTypeList);

            return this;
        }

        public List<Integer> getShowFileType() {
            return fileTypeList;
        }

        public Context getContext() {
            return context;
        }
    }
}
