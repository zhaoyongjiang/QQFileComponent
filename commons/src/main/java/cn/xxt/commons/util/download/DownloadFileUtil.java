package cn.xxt.commons.util.download;

import java.io.File;


/**
 * Created by Luke on 16/5/4.
 */
public class DownloadFileUtil {

    /**
     * 开始消息提示常量
     * */
    public static final int START_DOWNLOAD_MSG = 1;

    /**
     * 更新消息提示常量
     * */
    public static final int UPDATE_DOWNLOAD_MSG = 2;

    /**
     * 完成消息提示常量
     * */
    public static final int DOWNLOAD_SUCCESS_MSG = 3;

    public static final int DOWNLOAD_FAIL_MSG = 4;

    /**
     * 检验SDcard状态
     * @return boolean
     */
    public static boolean checkSDCard()
    {
        if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
        {
            return true;
        }else{
            return false;
        }
    }

    public static void deleteFile(String pathStr) {
        File file = new File(pathStr);
        deleteFile(file);
    }

    public static void deleteFile(File file) {
        // 判断文件是否存在
        if (file.exists()) {
            // 判断是否是文件
            if (file.isFile()) {
                // delete()方法 你应该知道 是删除的意思;
                file.delete();
            } else if (file.isDirectory()) {
                // 否则如果它是一个目录
                // 声明目录下所有的文件 files[];
                File[] files = file.listFiles();
                // 遍历目录下所有的文件
                for (int i = 0; i < files.length; i++) {
                    // 把每个文件 用这个方法进行迭代
                    deleteFile(files[i]);
                }
            }
            file.delete();
        }
    }

    /**
     * 获取文件名称
     * @param url
     * @return
     */
    public static  String getFileName(String url)
    {
        String name= null;
        try {
            name = url.substring(url.lastIndexOf("/")+1);
        } catch (Exception e) {
        }
        return name;
    }

}
