package cn.xxt.commons.util;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xxthxn on 16/3/9.
 */
public final class FileUtil {

    /**
     * 获取文件大小单位为B的double值
     */
    public static final int SIZETYPE_B = 1;
    /**
     * 获取文件大小单位为KB的double值
     */
    public static final int SIZETYPE_KB = 2;
    /**
     * 获取文件大小单位为MB的double值
     */
    public static final int SIZETYPE_MB = 3;
    /**
     * 获取文件大小单位为GB的double值
     */
    public static final int SIZETYPE_GB = 4;

    /**
     * 创建多级目录
     * @param path 目录
     * @return true：创建成功  false：创建失败
     */
    public static boolean makeDir(String path) {
        File filePath =  new File(path);
        if (!filePath.exists()) {
            boolean bFlag = filePath.mkdirs();
            if (!bFlag) {
                Log.e("FileUtil", "mkdirs failed, path " +path);
                return false;
            }
        }
        return true;
    }

    /**
     * 写文件
     * @param filePath  文件完整路径
     * @param fileContent 文件内容
     * @param encode 文件编码格式:GBK utf-8
     * @return false：写文件失败 true：写文件成功
     */
    public static boolean writeFile(String filePath, String fileContent, String encode) {
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filePath, true), encode));
            out.write(fileContent);
            out.close();
        } catch (FileNotFoundException e) {
            return false;
        }
        catch (IOException e){
            return false;
        }
        return true;

    }

    /**
     * 从文件绝对路径中提取文件名
     * @param absFileName：文件绝对路径 path+filename , 路径以/分开
     * @return “”：获取失败 非“”文件名
     */
    public static String getFileNameFromAbsName(String absFileName) {
        int index = absFileName.lastIndexOf("/");
        if (-1 == index) {
            return "";
        }
        return absFileName.substring(index + 1);
    }

    /**
     * 删除文件
     * @param fileName 文件名（含路径）
     * @return false表示删除失败，true删除成功
     */
    public static boolean deleteFile(String fileName) {
        if ("" .equals(fileName)) {
            return false;
        }

        File file = new File(fileName);
        if (file.exists()) {
            boolean d = file.delete();
            if (!d) {
                Log.e("FileUtil", "delete file failed, fileName " +fileName);
                return false;
            }
        }
        return true;
    }

    /**
     * 删除文件夹里的所有非文件夹的文件
     * @param pPath
     */
    public static void cleanDir(final String pPath) {
        File dir = new File(pPath);
        cleanDirWihtFile(dir);
    }

    public static void cleanDirWihtFile(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                cleanDirWihtFile(f);
            }
//            dir.delete();//如要保留文件夹，只删除文件，请注释这行
        } else if (dir.exists()) {
            dir.delete();
        }
    }

    public static String getLocalDir() {
        return Environment.getExternalStorageDirectory()
                + "/QQFileComponent/";
    }

    /**
     * 获取文件指定文件的指定单位的大小
     * @param filePath 文件路径
     * @param sizeType 获取大小的类型1为B、2为KB、3为MB、4为GB
     * @return double值的大小
     */
    public static double getFileOrFilesSize(String filePath, int sizeType){
        File file=new File(filePath);
        long blockSize=0;
        try {
            if(file.isDirectory()){
                blockSize = getFileSizes(file);
            }else{
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("获取文件大小","获取失败!");
        }
        return formetFileSize(blockSize, sizeType);
    }
    /**
     * 调用此方法自动计算指定文件或指定文件夹的大小
     * @param filePath 文件路径
     * @return 计算好的带B、KB、MB、GB的字符串
     */
    public static String getAutoFileOrFilesSize(String filePath){
        File file=new File(filePath);
        long blockSize=0;
        try {
            if(file.isDirectory()){
                blockSize = getFileSizes(file);
            }else{
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("获取文件大小","获取失败!");
        }
        return formetFileSize(blockSize);
    }
    /**
     * 获取指定文件大小
     * @param file
     * @return
     * @throws Exception
     */
    public static long getFileSize(File file) throws Exception
    {
        long size = 0;
        if (file.exists()){
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        }
        else{
            file.createNewFile();
            Log.e("获取文件大小","文件不存在!");
        }
        return size;
    }

    /**
     * 获取指定文件夹
     * @param f
     * @return
     * @throws Exception
     */
    public static long getFileSizes(File f) throws Exception
    {
        long size = 0;
        File[] flist = f.listFiles();
        for (int i = 0; i < flist.length; i++){
            if (flist[i].isDirectory()){
                size = size + getFileSizes(flist[i]);
            }
            else{
                size =size + getFileSize(flist[i]);
            }
        }
        return size;
    }
    /**
     * 转换文件大小
     * @param fileS
     * @return
     */
    public static String formetFileSize(long fileS)
    {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize="0B";
        if(fileS==0){
            return wrongSize;
        }
        if (fileS < 1024){
            fileSizeString = df.format((double) fileS) + "B";
        }
        else if (fileS < 1048576){
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        }
        else if (fileS < 1073741824){
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        }
        else{
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }
    /**
     * 转换文件大小,指定转换的类型
     * @param fileS
     * @param sizeType
     * @return
     */
    public static double formetFileSize(long fileS, int sizeType) {
        DecimalFormat df = new DecimalFormat("#.00");
        double fileSizeLong = 0;
        switch (sizeType) {
            case SIZETYPE_B:
                fileSizeLong = Double.valueOf(df.format((double) fileS));
                break;
            case SIZETYPE_KB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1024));
                break;
            case SIZETYPE_MB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1048576));
                break;
            case SIZETYPE_GB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1073741824));
                break;
            default:
                break;
        }
        return fileSizeLong;
    }

    /**
     * 根据绝对路径判断文件是否存在
     * @param filePath
     * @return
     */
    public static boolean fileIsExsit(String filePath) {
        try{
            File f = new File(filePath);
            if(!f.exists()){
                return false;
            }
        }catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 从Url中获取文件名
     * @param url
     * @return
     */
    public static String getFileNameFromUrl(String url) {
        try {
            int indexOfLastSprit = url.lastIndexOf("/");
            return url.substring(indexOfLastSprit + 1);
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * 屏蔽系统扫描此目录下的媒体文件
     * @param dir
     * @return
     */
    public static boolean hideMediaInDir(String dir) {
        boolean flag = true;

        try {
            String tempDir = getDirPathAfterFormat(dir);
            makeDir(tempDir);

            File nomedia = new File(tempDir + ".nomedia");
            if (!nomedia.exists()) {
                flag = nomedia.createNewFile();
            }
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 隐藏QQFileComponent路径下的部分文件路径，不对用户开放
     *
     * 使用：在homepage中oncreate中调用
     *
     * 拓展：如果QQFileComponent路径中有新的文件路径要隐藏，在HideDirEnum中假如拓展路径枚举即可。
     *
     * @return
     */
    public static boolean hideMedia() {
        boolean flag = true;
        String dir = Environment.getExternalStorageDirectory() + "/QQFileComponent/";

        HideDirEnum[] externalDirS = HideDirEnum.values();
        for (HideDirEnum external : externalDirS) {
            String tmpDir = StringUtil.connectStrings(dir, external.externalDir, "/");
            flag = flag & hideMediaInDir(tmpDir);
        }

        return flag;
    }

    public enum HideDirEnum {

        HIDE_DIR_ENUM_FILE("file"),
        HIDE_DIR_ENUM_TMP("tmp"),
        HIDE_DIR_ENUM_VOICE("voice"),
        HIDE_DIR_ENUM_VOICE_RECORD("voiceRecord");

        private String externalDir;
        HideDirEnum(String externalDir)
        {
            this.externalDir = externalDir;
        }
    }

    /**
     * 处理额外存储分区路径为标准写法，
     * 检测添加额外存储分区根路径、检测添加末尾的'/'
     * @param dir
     * @return
     */
    public static String getDirPathAfterFormat(String dir)
    {
        String tempDir = dir;

        if (!tempDir.startsWith(Environment.getExternalStorageDirectory().getPath())){
            if (dir.startsWith("/")) {
                tempDir = StringUtil.connectStrings(Environment.getExternalStorageDirectory().getPath()
                        , dir);
            } else {
                tempDir = StringUtil.connectStrings(Environment.getExternalStorageDirectory().getPath()
                        , "/", dir);
            }
        }

        if (!tempDir.endsWith("/"))
        {
            tempDir += "/";
        }

        return tempDir;
    }

    public static String getExt(String pathStr) {
        // 解析出url对应的资源后缀
        String extStr = "";

        int lastDotIndex = pathStr.lastIndexOf(".");
        if (lastDotIndex > 0) {
            extStr = pathStr.substring(lastDotIndex + 1);
        }

        return extStr;
    }

    public static String getSuffix(File file) {
        String suffix = "";
        try {
            // .前边贪婪匹配加上最后非.
            Pattern pattern = Pattern.compile("[\\S\\s]*(?<=\\.)([^.]*)");
            Matcher matcher = pattern.matcher(file.getName());
            if (matcher.find()) {
                suffix = matcher.group(1);
            }

        } catch (Exception e) {

        }

        return suffix;
    }

    public static String getSuffixFromUrl(String urlStr) {
        String suffix = "";
        try {
            // .前边贪婪匹配加上最后非.
            URL url = new URL(urlStr);
            Pattern pattern = Pattern.compile("[\\S\\s]*(?<=\\.)([^.]*)");
            Matcher matcher = pattern.matcher(url.getFile());
            if (matcher.find()) {
                suffix = matcher.group(1);
            }

        } catch (Exception e) {

        }

        return suffix;
    }

}
