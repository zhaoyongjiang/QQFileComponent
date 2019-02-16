package cn.xxt.commons.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5生成器
 * @author YuQing
 *
 */
public class Md5Util {
    /**
     * 默认的密码字符串组合，用来将字节转换成 16 进制表示的字符,apache校验下载的文件的正确性用的就是默认的这个组合
     */
    protected final static char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    protected static MessageDigest messagedigest =  null ;

    static {
        try {
            messagedigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nsaex) {
            System.err.println(Md5Util.class .getName()
                    + "初始化失败，MessageDigest不支持MD5Util。" );
        }
    }

    /**
     * 生成字符串的md5校验值
     *
     * @param s 字符串
     * @return  对应的md5码
     */
    public static String getMD5String(String s) {
        return getMD5String(s.getBytes());
    }

    /**
     * 生成字符串的md5校验值
     *
     * @param s 字符串
     * @return  对应的md5码
     */
    public static String getMD5StringBit16(String s) {
        return bit32ToBit16(getMD5String(s.getBytes()));
    }

    /**
     * 判断字符串的md5校验码是否与一个已知的md5码相匹配
     *
     * @param password 要校验的字符串
     * @param md5PwdStr 已知的md5校验码
     * @return  md5码是否一致，校验结果
     */
    public static boolean checkPassword(String password, String md5PwdStr) {
        String s = getMD5String(password);
        return s.equals(md5PwdStr);
    }

    /**
     * 生成文件的md5校验值
     *
     * @param filePath  文件路径
     * @return  文件的md5码
     */
    public static String getFileMD5String(String filePath){
        String md5 = "";

        File file = new File(filePath);

        try {
            InputStream fis = new FileInputStream(file);
            md5 = getFileMD5String(fis);

        } catch (FileNotFoundException e) {
        }


        return md5;
    }

    public static String getFileMD5StringBit16(String filePath){
        return bit32ToBit16(getFileMD5String(filePath));
    }

    /**
     * 生成文件的md5校验值
     * @param fis   文件输入流
     * @return  文件的md5值
     */
    public static String getFileMD5String(InputStream fis){
        String md5 = "";
        String confusedStr = "%bYk#y3EFLo$zBbemZ*6T%fn!&At04P1zm#LmzylJoq6WV*gSz1O05k&MURqcq!#";
        try {
            byte []buffer = new byte[1024];
            int  numRead;
            boolean done = false;
            while (!done) {
                numRead = fis.read(buffer);
                if (numRead > 0) {
                    messagedigest.update(buffer, 0 , numRead);
                } else {
                    buffer = confusedStr.getBytes();
                    messagedigest.update(buffer, 0 , buffer.length);
                    done = true;
                }
            }
            fis.close();
            md5 = bufferToHex(messagedigest.digest());
        } catch (Exception e) {
        }
        return md5;
    }

    public static String getFileMD5StringBit16(InputStream fis){
        return bit32ToBit16(getFileMD5String(fis));
    }

    public static String getMD5String(byte [] bytes) {
        messagedigest.update(bytes);
        return  bufferToHex(messagedigest.digest());
    }

    public static String getMD5StringBit16(byte [] bytes) {
        return bit32ToBit16(getMD5String(bytes));
    }

    private static String bufferToHex( byte  bytes[]) {
        return bufferToHex(bytes,0 ,bytes.length);
    }

    private   static  String bufferToHex( byte  bytes[],  int  m,  int  n) {
        StringBuffer stringbuffer = new  StringBuffer( 2  * n);
        int  k = m + n;
        for  ( int  l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }

        return stringbuffer.toString();
    }

    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = hexDigits[(bt &  0xf0 ) >>  4 ]; // 取字节中高 4 位的数字转换, >>> 为逻辑右移，将符号位一起右移,此处未发现两种符号有何不同
        char c1 = hexDigits[bt &  0xf ]; // 取字节中低 4 位的数字转换
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }

    private static String bit32ToBit16(String str) {
        if (str!=null && str.length() == 32) {
            str = str.substring(8,24);
        }

        return str;
    }

}

