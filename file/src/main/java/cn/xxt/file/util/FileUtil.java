package cn.xxt.file.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import cn.xxt.commons.util.Md5Util;
import cn.xxt.commons.util.StringUtil;
import cn.xxt.file.R;
import cn.xxt.file.internal.domain.FileInfo;
import cn.xxt.file.internal.domain.FileTypeEnum;
import cn.xxt.file.internal.domain.FolderInfo;
import cn.xxt.webview.ui.fileOpen.X5FileOpenUtil;


/**
 * Created by zyj on 2017/8/15.
 */

public class FileUtil {
    /****
     * 计算文件大小
     *
     * @param length
     * @return
     */
    public static String getFileSzie(double length) {
        if (length >= 1048576) {
            return (length / 1048576) + "MB";
        } else if (length >= 1024) {
            return (length / 1024) + "KB";
        } else if (length < 1024) {
            return length + "B";
        } else {
            return "0KB";
        }
    }

    public static String FormetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 字符串时间戳转时间格式
     *
     * @param timeStamp
     * @return
     */
    public static String getStrTime(String timeStamp) {
        String timeString = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 hh:mm");
        long l = Long.valueOf(timeStamp) * 1000;
        timeString = sdf.format(new Date(l));
        return timeString;
    }

    /**
     * 读取文件的最后修改时间的方法
     */
    public static String getFileLastModifiedTime(File f) {
        Calendar cal = Calendar.getInstance();
        long time = f.lastModified();
        SimpleDateFormat formatter = new
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        cal.setTimeInMillis(time);
        return formatter.format(cal.getTime());
    }

    /**
     * 获取扩展内存的路径
     *
     * @param mContext
     * @return
     */
    public static String getStoragePath(Context mContext) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * zyj : 20170815
     * 音频不按后缀名
     *
     *
     *
     * @param mContext
     * @param fileName
     * @return
     */
    public static int getFileTypeImageId(Context mContext, String fileName) {
        int id = 0;

        if (checkSuffix(fileName, new String[]{"mp3"})) {
            id = R.drawable.rc_ad_list_audio_icon;

        } else if (checkSuffix(fileName, new String[]{"wmv", "rmvb", "avi", "mp4"})) {
//            id = R.drawable.rc_ad_list_video_icon;
        } else if (checkSuffix(fileName, new String[]{"wav", "aac", "amr"})) {
//            id = R.drawable.rc_ad_list_video_icon;
        }
//        if (checkSuffix(fileName, mContext.getResources().getStringArray(R.array.rc_file_file_suffix)))
//            id = R.drawable.rc_ad_list_file_icon;
//        else if (checkSuffix(fileName, mContext.getResources().getStringArray(R.array.rc_video_file_suffix)))
//            id = R.drawable.rc_ad_list_video_icon;
//        else if (checkSuffix(fileName, mContext.getResources().getStringArray(R.array.rc_audio_file_suffix)))
//            id = R.drawable.rc_ad_list_audio_icon;
//        else
//            id = R.drawable.rc_ad_list_other_icon;
        return id;
    }

    public static int getResourceIdByFileType(Context context, int fileType) {
        int id = 0;
        if (fileType == FileTypeEnum.TYPE_DOC_WORD.getFileType()) {
            id = R.drawable.ic_word;
        } else if (fileType == FileTypeEnum.TYPE_DOC_EXCEL.getFileType()) {
            id = R.drawable.ic_excel;
        } else if (fileType == FileTypeEnum.TYPE_DOC_PDF.getFileType()) {
            id = R.drawable.ic_pdf;
        } else if (fileType == FileTypeEnum.TYPE_DOC_PPT.getFileType()) {
            id = R.drawable.ic_ppt;
        } else if (fileType == FileTypeEnum.TYPE_AUDIO.getFileType()) {
            id = R.drawable.ic_music;
        } else if (fileType == FileTypeEnum.TYPE_IMAGE.getFileType()) {

        } else if (fileType == FileTypeEnum.TYPE_VIDEO.getFileType()) {
            //todo 视频图标
            id = R.drawable.rc_file_icon_video;
        } else if (fileType == FileTypeEnum.TYPE_OTHER_TXT.getFileType()) {
            id = R.drawable.ic_txt;
        } else if (fileType == FileTypeEnum.TYPE_OTHER_ZIP.getFileType()) {
            id = R.drawable.rc_ad_list_other_icon;
        } else if (fileType == FileTypeEnum.TYPE_OTHER_RAR.getFileType()) {
            id = R.drawable.rc_ad_list_other_icon;
        } else if (fileType == FileTypeEnum.TYPE_OTHER_APK.getFileType()) {
            id = R.drawable.rc_ad_list_other_icon;
        }
        return id;
    }

    public static int getFileIconCodeByFileType(Context context, int fileType) {
        int iconCodeId = 0;
        if (fileType == FileTypeEnum.TYPE_DOC_WORD.getFileType()) {
            iconCodeId = R.string.iconfont_word;
        } else if (fileType == FileTypeEnum.TYPE_DOC_EXCEL.getFileType()) {
            iconCodeId = R.string.iconfont_excel;
        } else if (fileType == FileTypeEnum.TYPE_DOC_PDF.getFileType()) {
            iconCodeId = R.string.iconfont_pdf;
        } else if (fileType == FileTypeEnum.TYPE_DOC_PPT.getFileType()) {
            iconCodeId = R.string.iconfont_ppt;
        } else if (fileType == FileTypeEnum.TYPE_AUDIO.getFileType()) {
            iconCodeId = R.string.iconfont_auido;
        } else if (fileType == FileTypeEnum.TYPE_IMAGE.getFileType()) {

        } else if (fileType == FileTypeEnum.TYPE_VIDEO.getFileType()) {
            iconCodeId = R.string.iconfont_video;
        } else if (fileType == FileTypeEnum.TYPE_OTHER_TXT.getFileType()) {
            iconCodeId = R.string.iconfont_txt;
        } else if (fileType == FileTypeEnum.TYPE_OTHER_ZIP.getFileType()) {
            iconCodeId = R.string.iconfont_zip;
        } else if (fileType == FileTypeEnum.TYPE_OTHER_RAR.getFileType()) {
            iconCodeId = R.string.iconfont_zip;
        } else if (fileType == FileTypeEnum.TYPE_OTHER_APK.getFileType()) {
            iconCodeId = R.string.iconfont_unknown;
        } else {
            iconCodeId = R.string.iconfont_other;
        }
        return iconCodeId;
    }

    public static int getFileIconColorByFileType(Context context, int fileType) {
        int iconColor = 0;
        if (fileType == FileTypeEnum.TYPE_DOC_WORD.getFileType()) {
            iconColor = R.color.color_word;
        } else if (fileType == FileTypeEnum.TYPE_DOC_EXCEL.getFileType()) {
            iconColor = R.color.color_excel;
        } else if (fileType == FileTypeEnum.TYPE_DOC_PDF.getFileType()) {
            iconColor = R.color.color_pdf;
        } else if (fileType == FileTypeEnum.TYPE_DOC_PPT.getFileType()) {
            iconColor = R.color.color_ppt;
        } else if (fileType == FileTypeEnum.TYPE_AUDIO.getFileType()) {
            iconColor = R.color.color_audio;
        } else if (fileType == FileTypeEnum.TYPE_IMAGE.getFileType()) {

        } else if (fileType == FileTypeEnum.TYPE_VIDEO.getFileType()) {
            iconColor = R.color.color_video;
        } else if (fileType == FileTypeEnum.TYPE_OTHER_TXT.getFileType()) {
            iconColor = R.color.color_txt;
        } else if (fileType == FileTypeEnum.TYPE_OTHER_ZIP.getFileType()) {
            iconColor = R.color.color_zip;
        } else if (fileType == FileTypeEnum.TYPE_OTHER_RAR.getFileType()) {
            iconColor = R.color.color_zip;
        } else if (fileType == FileTypeEnum.TYPE_OTHER_APK.getFileType()) {
            iconColor = R.color.color_unknown;
        } else {
            iconColor = R.color.color_other;
        }
        return iconColor;
    }

    public static boolean checkSuffix(String fileName,
                                      String[] fileSuffix) {
        for (String suffix : fileSuffix) {
            if (fileName != null) {
                if (fileName.toLowerCase().endsWith(suffix)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 文件过滤,将手机中隐藏的文件给过滤掉
     */
    public static File[] fileFilter(File file) {
        File[] files = file.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return !pathname.isHidden();
            }
        });
        return files;
    }


    public static List<FileInfo> getFilesInfo(List<String> fileDir, Context mContext) {
        List<FileInfo> mlist = new ArrayList<>();
        for (int i = 0; i < fileDir.size(); i++) {
            if (new File(fileDir.get(i)).exists()) {
                mlist = FilesInfo(new File(fileDir.get(i)), mContext);
            }
        }
        return mlist;
    }

    private static List<FileInfo> FilesInfo(File fileDir, Context mContext) {
        List<FileInfo> videoFilesInfo = new ArrayList<>();
        File[] listFiles = fileFilter(fileDir);
        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.isDirectory()) {
                    FilesInfo(file, mContext);
                } else {
                    FileInfo fileInfo = getFileInfoFromFile(file);
                    videoFilesInfo.add(fileInfo);
                }
            }
        }
        return videoFilesInfo;
    }

    public static List<FileInfo> getFileInfosFromFileArray(File[] files) {
        List<FileInfo> fileInfos = new ArrayList<>();
        for (File file : files) {
            FileInfo fileInfo = getFileInfoFromFile(file);
            fileInfos.add(fileInfo);
        }
        Collections.sort(fileInfos, new FileNameComparator());
        return fileInfos;
    }

    /**
     * 根据文件名进行比较排序
     */
    public static class FileNameComparator implements Comparator<FileInfo> {
        protected final static int
                FIRST = -1,
                SECOND = 1;

        @Override
        public int compare(FileInfo lhs, FileInfo rhs) {
            if (lhs.isDirectory() || rhs.isDirectory()) {
                if (lhs.isDirectory() == rhs.isDirectory()) {
                    return lhs.getFileName().compareToIgnoreCase(rhs.getFileName());
                } else if (lhs.isDirectory()) {
                    return FIRST;
                } else {
                    return SECOND;
                }
            }
            return lhs.getFileName().compareToIgnoreCase(rhs.getFileName());
        }
    }

    public static FileInfo getFileInfoFromFile(File file) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileName(file.getName());
        fileInfo.setFileLocalPath(file.getPath());
        fileInfo.setFileSize(file.length());
        fileInfo.setDirectory(file.isDirectory());
        fileInfo.setUpdateDate(file.lastModified());
        String fileName = file.getName();
        if (fileName.length() > 0) {
            fileInfo.setFileSuffix(getFileSuffixFromFileName(fileName));
        }
        return fileInfo;
    }

    public static String getFileSuffixFromFileName(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            String fileSuffix = fileName.substring(lastDotIndex + 1);
            return fileSuffix;
        }
        return "";
    }

    public static String getfileNamePrefixFromFileName(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            String fileSuffix = fileName.substring(0, lastDotIndex);
            return fileSuffix;
        }
        return "";
    }

    public static List<FolderInfo> queryFolderInfo(Context context, List<Uri> mlist) {
        List<FolderInfo> folderInfos = new ArrayList<>();

        for (int i = 0; i < mlist.size(); i++) {
            String[] projection = new String[]{
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.DATE_MODIFIED
            };
            Cursor cursor = context.getContentResolver().query(
                    mlist.get(i),
                    projection, null,
                    null, projection[2] + " DESC");

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int dataindex = cursor
                            .getColumnIndex(MediaStore.Files.FileColumns.DATA);
                    int nameindex = cursor
                            .getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME);
                    int timeindex = cursor
                            .getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED);
                    do {
                        FileInfo fileInfo = new FileInfo();
                        String path = cursor.getString(dataindex);
                        String name = cursor.getString(nameindex);
                        long time = cursor.getLong(timeindex);
                        fileInfo.setFileSize(new File(path).length());
                        fileInfo.setFileLocalPath(path);
                        fileInfo.setFileName(name);
                        fileInfo.setUpdateDate(time);
                        FolderInfo folderInfo = getImageFolder(path, folderInfos);
                        folderInfo.getFileInfos().add(fileInfo);
                    } while (cursor.moveToNext());
                }
            }
            cursor.close();
        }
        return folderInfos;

    }

    public static List<FileInfo> queryFilerInfo(Context context, List<Uri> mlist, String selection, String[] selectionArgs) {
        List<FileInfo> fileInfos = new ArrayList<>();
        for (int i = 0; i < mlist.size(); i++) {
            String[] projection = new String[]{
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.TITLE,
                    MediaStore.Files.FileColumns.DATE_MODIFIED
            };
            Cursor cursor = context.getContentResolver().query(
                    mlist.get(i),
                    projection, selection,
                    selectionArgs, projection[2] + " DESC");

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int dataindex = cursor
                            .getColumnIndex(MediaStore.Files.FileColumns.DATA);
                    int nameindex = cursor
                            .getColumnIndex(MediaStore.Files.FileColumns.TITLE);
                    int timeindex = cursor
                            .getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED);
                    do {
                        FileInfo fileInfo = new FileInfo();
                        String path = cursor.getString(dataindex);
                        String name = cursor.getString(nameindex);
                        long time = cursor.getLong(timeindex);
                        fileInfo.setFileSize(new File(path).length());
                        fileInfo.setFileLocalPath(path);
                        fileInfo.setFileName(name);
                        fileInfo.setUpdateDate(time);
                        fileInfos.add(fileInfo);

                    } while (cursor.moveToNext());
                }
            }
            cursor.close();
        }
        return fileInfos;

    }

    public static FolderInfo getImageFolder(String path, List<FolderInfo> imageFolders) {
        File imageFile = new File(path);
        File folderFile = imageFile.getParentFile();

        for (FolderInfo folder : imageFolders) {
            if (folder.getName().equals(folderFile.getName())) {
                return folder;
            }
        }
        FolderInfo newFolder = new FolderInfo();
        newFolder.setName(folderFile.getName());
        newFolder.setPath(folderFile.getAbsolutePath());
        imageFolders.add(newFolder);
        return newFolder;
    }

    public static List<FolderInfo> fileinfoTypeWithDate(List<FileInfo> fileInfoList){
        List<FolderInfo> folderInfoList = new ArrayList<>();

        long timeNodeToday = DateUtil.timeNodeTodayFirst();
        long timeNodeYesterday = DateUtil.timeNodeYesterdayFirst();
        long timeNodeOneWeekAgo = DateUtil.timeNodeDayAgo(7);
        long timeNodeMonthAgo = DateUtil.timeNodeMonthAgo(1);
        long timeNodeSixMonthAgo = DateUtil.timeNodeMonthAgo(6);

        List<FileInfo> fileInfosToday = new ArrayList<>();
        List<FileInfo> fileInfosYestoday = new ArrayList<>();
        List<FileInfo> fileInfosWithinWeek = new ArrayList<>();
        List<FileInfo> fileInfosWithinMonth = new ArrayList<>();
        List<FileInfo> fileInfosWinthinSixMonth = new ArrayList<>();

        for (int i = 0; i < fileInfoList.size(); i ++) {
            FileInfo fileInfo = fileInfoList.get(i);
            long fileTime = fileInfo.getUpdateDate();
            if (fileTime == 0) {
                fileTime = fileInfo.getCreateDate();
            }
            if (fileTime > timeNodeToday) {
                //今天
                fileInfosToday.add(fileInfo);
            } else if (fileTime > timeNodeYesterday) {
                //昨天
                fileInfosYestoday.add(fileInfo);
            } else if (fileTime > timeNodeOneWeekAgo) {
                //一周内
                fileInfosWithinWeek.add(fileInfo);
            } else if (fileTime > timeNodeMonthAgo) {
                //一月内
                fileInfosWithinMonth.add(fileInfo);
            } else if (fileTime > timeNodeSixMonthAgo) {
                //六月内
                fileInfosWinthinSixMonth.add(fileInfo);
            }
        }

        if (fileInfosToday.size() > 0) {
            FolderInfo folderInfo = new FolderInfo();
            folderInfo.setName("今天");
            folderInfo.setFileInfos(fileInfosToday);
            folderInfoList.add(folderInfo);
        }
        if (fileInfosYestoday.size() > 0) {
            FolderInfo folderInfo = new FolderInfo();
            folderInfo.setName("昨天");
            folderInfo.setFileInfos(fileInfosYestoday);
            folderInfoList.add(folderInfo);
        }
        if (fileInfosWithinWeek.size() > 0) {
            FolderInfo folderInfo = new FolderInfo();
            folderInfo.setName("一周内");
            folderInfo.setFileInfos(fileInfosWithinWeek);
            folderInfoList.add(folderInfo);
        }
        if (fileInfosWithinMonth.size() > 0) {
            FolderInfo folderInfo = new FolderInfo();
            folderInfo.setName("一个月内");
            folderInfo.setFileInfos(fileInfosWithinMonth);
            folderInfoList.add(folderInfo);
        }
        if (fileInfosWinthinSixMonth.size() > 0) {
            FolderInfo folderInfo = new FolderInfo();
            folderInfo.setName("6个月内");
            folderInfo.setFileInfos(fileInfosWinthinSixMonth);
            folderInfoList.add(folderInfo);
        }

        return folderInfoList;
    }

    public static boolean isFileExits(FileInfo fileInfo) {
        boolean exits = false;
        String fileLocalPath = fileInfo.getFileLocalPath();
        if (fileLocalPath != null && fileLocalPath.length() > 0) {
            File file = new File(fileLocalPath);
            if (file != null && file.exists()) {
                exits = true;
            } else {
                exits = false;
            }
        } else {
            exits = false;
        }
        return exits;
    }

    public static String getFileSaveDir() {
        return StringUtil.connectStrings(Environment.getExternalStorageDirectory().getPath(),
                "/QQFileComponent/");
    }

    /**
     * 文件下载，获取文件保存到本地的名字：如：
     *
     * xxx.apk, xxx(1).apk......
     *
     * 入参约定：
     *
     * @param fileName ： 文件名：xxx.apk   带后缀
     * @param url ：文件url。url字符串
     * @return
     */
    public static String getFileSaveName(String fileName, String url) {
        if (fileName == null || fileName.length() == 0) {
//            return String.valueOf(System.currentTimeMillis());
            String mdgFileName = Md5Util.getMD5String(url) + "." + FileUtil.getFileSuffixFromFileName(url);
            return getFileSaveName(mdgFileName, null);
        }
        String suffix = "." + FileUtil.getFileSuffixFromFileName(fileName);
        String dir = getFileSaveDir();
        int suffix_no = 1;
        String fileNamePrefix = FileUtil.getfileNamePrefixFromFileName(fileName);
        String filePath = dir + fileNamePrefix + suffix;
        File downloadFile = new File(filePath);
        while(downloadFile.exists())
        {
            downloadFile = new File(dir,fileNamePrefix + "(" + suffix_no + ")" + suffix);
            suffix_no++;
        }

        String name = fileNamePrefix;
        if (suffix_no > 1) {
            name = fileNamePrefix + "(" + (suffix_no - 1) + ")";
        }

        return name + suffix;
    }

    public static void deleteFile(String pathStr) {
        File file = new File(pathStr);
        deleteFile(file);
    }

    public static void deleteFile(File file) {
        if (file.exists()) {
            // 判断文件是否存在
            if (file.isFile()) {
                // 判断是否是文件
                // delete()方法 你应该知道 是删除的意思;
                file.delete();
            } else if (file.isDirectory()) {
                // 否则如果它是一个目录
                // 声明目录下所有的文件 files[];
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    // 遍历目录下所有的文件
                    // 把每个文件 用这个方法进行迭代
                    deleteFile(files[i]);
                }
            }
            file.delete();
        }
    }

    public static String getMIMEType(File file) {
        String type = null;
        String suffix = file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length());
        if (suffix.equals("apk")) {
            type = "application/vnd.android.package-archive";
        } else {
            // /*如果无法直接打开，就跳出软件列表给用户选择 */
            type = "*/*";
        }
        return type;
    }

    public static int analyzeFileTypeWithFileUrl(FileInfo fileInfo) {
        int fileType = fileInfo.getFileType();

        if (fileType <= 0) {
            String fileUrl = fileInfo.getFileLocalPath();
            if (!StringUtil.isEmpty(fileUrl)) {
                String ext = FileUtil.getFileSuffixFromFileName(fileUrl).toLowerCase();

                switch (ext) {
                    case "doc":
                    case "docx":
                    case "dot":
                    case "dotx":
                        fileInfo.setFileType(FileTypeEnum.TYPE_DOC_WORD.getFileType());
                        break;
                    case "xls":
                    case "xlsx":
                        fileInfo.setFileType(FileTypeEnum.TYPE_DOC_EXCEL.getFileType());
                        break;
                    case "ppt":
                    case "pptx":
                        fileInfo.setFileType(FileTypeEnum.TYPE_DOC_PPT.getFileType());
                        break;
                    case "pdf":
                        fileInfo.setFileType(FileTypeEnum.TYPE_DOC_PDF.getFileType());
                        break;
                    case "mp3":
                    case "wav":
                    case "aac":
                    case "m4a":
                    case "mp4":
                        fileInfo.setFileType(FileTypeEnum.TYPE_AUDIO.getFileType());
                        break;
                    case "zip":
                        fileInfo.setFileType(FileTypeEnum.TYPE_OTHER_ZIP.getFileType());
                        break;
                    case "rar":
                        fileInfo.setFileType(FileTypeEnum.TYPE_OTHER_RAR.getFileType());
                        break;
                    case "txt":
                        fileInfo.setFileType(FileTypeEnum.TYPE_OTHER_TXT.getFileType());
                        break;
                    default:
                        break;
                }
            }
        }


        return fileInfo.getFileType();
    }

    public static X5FileOpenUtil.FileTypeEnum matchFileType2X5FileType(FileInfo fileInfo) {
        X5FileOpenUtil.FileTypeEnum type = X5FileOpenUtil.FileTypeEnum.FILE_TYPE_ENUM_OFFICE;

        if (fileInfo.fileType == FileTypeEnum.TYPE_OTHER_RAR.getFileType()
                || fileInfo.fileType == FileTypeEnum.TYPE_OTHER_ZIP.getFileType()) {
            //压缩包
            type = X5FileOpenUtil.FileTypeEnum.FILE_TYPE_ENUM_URL;
        } else if (fileInfo.fileType == FileTypeEnum.TYPE_VIDEO.getFileType()) {
            //视频
            type = X5FileOpenUtil.FileTypeEnum.FILE_TYPE_ENUM_VIDEO;
        } else if (fileInfo.fileType == FileTypeEnum.TYPE_AUDIO.getFileType()) {
            type = X5FileOpenUtil.FileTypeEnum.FILE_TYPE_ENUM_AUDIO;
        } else {
            //doc
            type = X5FileOpenUtil.FileTypeEnum.FILE_TYPE_ENUM_OFFICE;
        }

        return type;
    }
}
