package cn.xxt.commons.util;

import android.net.Uri;

import com.google.common.io.CharStreams;

import org.apache.commons.validator.routines.UrlValidator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String操作公共函数
 * Created by xxt-hxn on 16/3/14.
 */
public final class StringUtil {

    /**使用预编译加快正则表达式匹配速度 zhq 2017-11-14*/
    private static Pattern NUMBER_PATTERN = Pattern.compile("-?[0-9]*.?[0-9]*");
    private static String REGXP_FOR_HTML = "<([^>]*)>";
    private static Pattern HTML_PATTERN = Pattern.compile(REGXP_FOR_HTML);
    private static Pattern MOBILE_PATTERN = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$");

    /**
     * 字符串拼接
     * @param subStrings 可变字符串数组
     * @return 拼接后的字符串
     */
    public static String connectStrings(String... subStrings) {
        StringBuilder sb = new StringBuilder();
        for (String str : subStrings) {
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * 返回指定长度的带后缀字符串的子字符串，如果字符串的长度比指定的值小，则返回原字符串
     * 是getStringSub（String str,int length)方法的扩展
     * */
    public static String getStringSubSuffix(String str,int length,String suffix){
        if(str==null) {
            return null;
        }
        String substr = str;
        int len = str.length();
        if(len > length) {
            substr = str.substring(0, length) + suffix;
        }
        return substr;
    }

    /**
     * 将驼峰式命名的字符串转换为下划线大写方式。如果转换前的驼峰式命名的字符串为空，则返回空字符串。</br>
     * 例如：HelloWorld->hello_world
     * @param name 转换前的驼峰式命名的字符串
     * @return 转换后下划线大写方式命名的字符串
     */
    public static String camel2Underscore(String name) {
        StringBuilder result = new StringBuilder();
        if (name != null && name.length() > 0) {
            // 将第一个字符处理成小写
            result.append(name.substring(0, 1).toLowerCase());
            // 循环处理其余字符
            for (int i = 1; i < name.length(); i++) {
                String s = name.substring(i, i + 1);
                // 在大写字母前添加下划线
                if (s.equals(s.toUpperCase()) && !Character.isDigit(s.charAt(0))) {
                    result.append("_");
                }
                // 其他字符直接转成小写
                result.append(s.toLowerCase());
            }
        }
        return result.toString();
    }

    /**
     * 将下划线大写方式命名的字符串转换为驼峰式。如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。</br>
     * 例如：HELLO_WORLD->helloWorld
     * @param name 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     */
    public static String underscore2Camel(String name) {
        StringBuilder result = new StringBuilder();
        // 快速检查
        if (name == null || name.isEmpty()) {
            // 没必要转换
            return "";
        } else if (!name.contains("_")) {
            // 不含下划线，仅将首字母小写
            return name.substring(0, 1).toLowerCase() + name.substring(1);
        }
        // 用下划线将原始字符串分割
        String[] camels = name.split("_");
        for (String camel :  camels) {
            // 跳过原始字符串中开头、结尾的下换线或双重下划线
            if (camel.isEmpty()) {
                continue;
            }
            // 处理真正的驼峰片段
            if (result.length() == 0) {
                // 第一个驼峰片段，全部字母都小写
                result.append(camel.toLowerCase());
            } else {
                // 其他的驼峰片段，首字母大写
                result.append(camel.substring(0, 1).toUpperCase());
                result.append(camel.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }

    /**
     * 根据传递的参数将String转换成Json格式的数据
     * @param params 键值对
     * @return
     */
    public static String transformStringToJson(String... params) {
        int count = params.length;
        String ret;
        if (count == 0) {
            ret = "{}";
        } else {
            StringBuffer sb = new StringBuffer("\"{");
            if (count%2 == 0) {
                for (int i=0;i<count;i++) {
                    sb.append(params[i]);
                    sb.append(":");
                    sb.append(params[++i]);
                    if (i == count-1) {
                        sb.append("}\"");
                    } else {
                        sb.append(",");
                    }
                }
            } else {
                for (int i=0;i<count-1;i++) {
                    sb.append(params[i]);
                    sb.append(":");
                    sb.append(params[++i]);
                    sb.append(",");
                }
                sb.append("");
                sb.append(params[count-1]);
                sb.append(": }\"");
            }
            ret = sb.toString();
        }
        return ret;
    }

    /**
     * 将数字转换成汉语文字
     *
     * @param num
     * @return
     */
    public static String num2China(int num) {
        String str = "";
        int size = (num + "").length();
        if (size == 1) {
            str = singleNum2China(num);
        }else if (size == 2) {
            str = singleNum2China(num / 10) + "十";
            if (num % 10 != 0) {
                str += singleNum2China(num % 10);
            }
        } else if (size == 3) {
            str = singleNum2China(num / 100) + "百";
            if (num % 100 != 0) {
                int num1 = num % 100;
                str += singleNum2China(num1 / 10) + "十";
                if (num % 10 != 0) {
                    str += singleNum2China(num1 % 10);
                }
            }
        }
        return str;
    }

    private static String singleNum2China(int a) {
        StringBuffer sb = new StringBuffer();
        String[] str = new String[] { "零", "一", "二", "三", "四", "五", "六", "七",
                "八", "九" };
        return sb.append(str[a]).toString();
    }

    /**
     * tzhk 2012-5-8 19:21
     * 传参到html页面时替换敏感字符
     * */
    public static String getReplacedStringForTransfer(String original)
    {
        String dealed = original;
        dealed = dealed.replace("\\", "\\\\");
        dealed = dealed.replace("\"", "\\\"");
        dealed = dealed.replace("\n", "");
        dealed = dealed.replace("\r", "");
        return dealed;
    }

    /**
     * 判断String是否是数
     *
     * @param str
     * @return
     */
    public static Boolean isNumeric(String str)
    {
        if(str==null || "".equals(str)) {
            return false;
        }
        return NUMBER_PATTERN.matcher(str).matches();
    }

    /**
     * 判断String是否是JSON格式 简单的判断一下是以‘{’开头以‘}’结尾 或者以‘[’开头以‘]’结尾
     *
     * @param str
     * @return
     */
    public static Boolean isJSON(String str)
    {
        if(str==null || "".equals(str)) {
            return false;
        }
        return ((str.startsWith("{") && str.endsWith("}"))
                || (str.startsWith("[") && str.endsWith("]")));
    }

    /**
     * unicode转UTF8
     *
     * @param unicodeString
     * @return
     */
    public static String unicode2UTF8(String unicodeString) {
        char aChar;
        int len = unicodeString.length();
        StringBuffer outBuffer = new StringBuffer(len);
        for (int x = 0; x < len;) {
            aChar = unicodeString.charAt(x++);
            if (aChar == '\\') {
                aChar = unicodeString.charAt(x++);
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = unicodeString.charAt(x++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed   \\uxxxx   encoding.");
                        }

                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't') {
                        aChar = '\t';
                    } else if (aChar == 'r') {
                        aChar = '\r';
                    } else if (aChar == 'n') {
                        aChar = '\n';
                    } else if (aChar == 'f') {
                        aChar = '\f';
                    }
                    outBuffer.append(aChar);
                }
            } else {
                outBuffer.append(aChar);
            }
        }
        return outBuffer.toString();
    }

    /**
     * trim方法，去除首尾空格
     * @param input
     * @return String
     * */
    public static String trim(String input) {
        return input != null ? input.trim() : input;
    }

    /**
     * filter，替换半角单引号、双引号为全角
     * @param input
     * @return String
     */
    private static String filter(String input) {
        if (input == null || input.length() == 0) {
            return input;
        }

        return input.replaceAll("'", "＇").replaceAll("\"", "＂");
    }

    /**
     * changeCode全角转半角
     * */
    private static String changeCode(String input) {
        if(input == null || input.length() == 0) {
            return input;
        }

        StringBuffer outputBuffer = new StringBuffer("");
        String str = "";
        byte[] b = null;

        for(int i = 0; i < input.length(); i++) {
            try {
                str = input.substring(i, i+1);
                if ("　".equals(str)) {
                    str = " ";
                }

                b = str.getBytes("unicode");
            } catch(UnsupportedEncodingException e) {
            }

            if(b[3] == -1) {
                b[2] = (byte)(b[2] + 32);
                b[3] = 0;

                try {
                    outputBuffer.append(new String(b,"unicode"));
                } catch(UnsupportedEncodingException e) {
                }

            } else {
                outputBuffer.append(str);
            }
        }

        return outputBuffer.toString();
    }

    /**
     * 常见的对字符串处理集合方法
     * @param input
     * @param changeCode 是否转换全角为半角
     * @param trim 是否进行trim操作
     * @param filter 是否进行filter替换
     * @return String
     * */
    public static String commonDeal(String input, boolean changeCode, boolean trim, boolean filter) {
        if (input == null || input.length() == 0) {
            return input;
        }
        if (changeCode) {
            input = changeCode(input);
        }

        if (trim) {
            input = trim(input);
        }

        if (filter) {
            input = filter(input);
        }

        return input;
    }

    /**
     * List转换String
     *
     * @param list
     *            :需要转换的List
     * @return String转换后的字符串
     */
    public static String listToString(List<String> list, String devider) {
        StringBuffer sb = new StringBuffer();
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) == null || list.get(i) == "") {
                    continue;
                }
                sb.append(list.get(i));
                if (i != list.size() - 1) {
                    sb.append(devider);
                }
            }
        }
        return sb.toString();
    }


    /**
     * 过滤所有以<开头以>结尾的标签
     *
     * @param str
     * @return
     */
    public static String filterHtml(String str) {
        Matcher matcher = HTML_PATTERN.matcher(str);
        StringBuffer sb = new StringBuffer();
        boolean result1 = matcher.find();
        while (result1) {
            matcher.appendReplacement(sb, "");
            result1 = matcher.find();
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


    public static boolean isUtf8CharsetInHeader(String contentTypeHeader){
        String pattern = ".*charset.*utf.*";
        return Pattern.matches(pattern,contentTypeHeader);
    }



    public static boolean isEmpty(String str) {
        return (str==null || str.isEmpty());
    }

    public static boolean isEmpty(Object o) {
        return (o==null || o.toString().isEmpty());
    }

    /**
     * InputStream 转 String
     * @param inputStream 输入流
     * @return      转换后的字符串
     * @throws IOException
     */
    public static String inputStreamToString(InputStream inputStream) throws IOException{
        //stream读取一遍之后，游标就到末尾了，我们在这函数里，也不知道它的长度，所以也没办法用mark和reset来重复利用，
        //所以就是拷贝之后重复利用
        //根据编码转字符串
        String text = "";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) > -1 ) {
            baos.write(buffer, 0, len);
        }
        baos.flush();

        if (baos.size()>0) {
            InputStream stream1 = new ByteArrayInputStream(baos.toByteArray());
            InputStream stream2 = new ByteArrayInputStream(baos.toByteArray());

            //获取字符串编码
            ParseEncoding parse = new ParseEncoding();
            boolean isUtf = parse.isUtf(stream1);

            if (isUtf) {
                text = CharStreams.toString(new InputStreamReader(stream2));
            } else {
                text = CharStreams.toString(new InputStreamReader(stream2, "gbk"));
            }
            stream1.close();
            stream2.close();
        }

        baos.close();

        return text;
    }

    /**
     * 是否是手机号码
     * @param mobiles   手机号码字符串
     * @return          true：是，false：不是
     */
    public static boolean isMobile(String mobiles) {
        Matcher m = MOBILE_PATTERN.matcher(mobiles);
        return m.matches();
    }

    /**
     * 返回URL编码的字符串
     * @param orginalStr
     * @param encoder
     * @return
     */
    public static String getURLEncodeString(String orginalStr, String encoder) {
        if (orginalStr == null || encoder == null || "".equals(encoder)) {
            return "";
        }
        try {
            return URLEncoder.encode(orginalStr, encoder);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    /**
     * 收发信息中批量删除，将id列表注转为字符串
     * 形如：（1,2,3）
     * @param list
     * @return
     */
    public static String longListToInSqlString(List<Long> list) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("(");
        for (int i = 0; i < list.size(); i++) {
            long id = list.get(i);
            stringBuffer.append(id);
            if (i < list.size() - 1) {
                stringBuffer.append(",");
            }
        }
        stringBuffer.append(")");

        return stringBuffer.toString();
    }

    /**
     * 收发信息中批量删除，将id列表注转为字符串
     * 形如：（"1","2","3"）
     * @param strList
     * @return
     */
    public static String strListToInSqlString(List<String> strList) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("(");
        for (int i = 0; i < strList.size(); i++) {
            String id = strList.get(i);
            stringBuffer.append(id);
            if (i < strList.size() - 1) {
                stringBuffer.append(",");
            }
        }
        stringBuffer.append(")");

        return stringBuffer.toString();
    }




    /**
     * 处理xxt几个自有域名链接转成https
     * @param urlStr 链接地址
     * @return
     */
    public static String urlStr2HttpsUrlStr(String urlStr) {
        if (isFCDomain(urlStr)) {
            if (SwitchOfHttps.isCloseTestHttps() && !isXXTLoginDomain(urlStr)) {
                //首先判断测试环境开关,如果是不使用https直接转为http
                urlStr = str2Http(urlStr);
            } else {
                if (SwitchOfHttps.isCloseHttps() && !isXXTLoginDomain(urlStr)) {
                    //判断正式开关，如果不使用https转为http
                    urlStr = str2Http(urlStr);
                } else {
                    urlStr = str2Https(urlStr);
                }
            }
        }

        return urlStr;
    }

    /**
     * 处理xxt几个自有域名链接转成http
     * @param urlStr 链接地址
     * @return
     */
    public static String urlStr2HttpUrlStr(String urlStr) {
        if (isFCDomain(urlStr)) {
            urlStr = str2Http(urlStr);
        }

        return urlStr;
    }

    /**
     * 判断是否是指定域名
     * @param urlStr 链接地址
     * @return
     */
    public static boolean isFCDomain(String urlStr) {
        boolean isFCDomain = false;

        if (!isEmpty(urlStr)) {
            try {
                Uri uri = Uri.parse(urlStr);
                //针对以下域名，使用http的转https，没有协议的直接加htpps
                String hostString = uri.getHost();
                if (hostString == null) {
                    //没有协议的，加上协议
                    urlStr = "http://" + urlStr;
                    uri = Uri.parse(urlStr);
                    hostString = uri.getHost();
                }
                //image.xxx.xx/mobile/image/home_test.png 如果是这种没有协议的地址，
                // hostString 会直接返回null
            } catch (Exception e) {

            }
        }

        return isFCDomain;
    }

    public static boolean isXXTLoginDomain(String urlStr) {
        boolean isXXTLoginDomain = false;

        if (!isEmpty(urlStr)) {
            try {
                Uri uri = Uri.parse(urlStr);
                //针对以下域名，使用http的转https，没有协议的直接加htpps
                String hostString = uri.getHost();
                if (hostString == null) {
                    //没有协议的，加上协议
                    urlStr = "http://" + urlStr;
                    uri = Uri.parse(urlStr);
                    hostString = uri.getHost();
                }
            } catch (Exception e) {

            }
        }

        return isXXTLoginDomain;
    }

    /**
     * 将链接转为https://
     * @param oldStr old Str
     */
    private static String str2Https(String oldStr) {
        String newStr = oldStr;
        if (newStr != null && !newStr.isEmpty()) {
            try {
                Uri uri = Uri.parse(newStr);
                String scheme = uri.getScheme();
                if (scheme == null) {
                    // 不是http 和 https的会直接返回null 的增加https协议
                    newStr = "https://" + newStr;
                } else if (scheme.equals("http")) {
                    //http 转 https
                    newStr = newStr.replace("http://", "https://");
                } else if (scheme.equals("https")) {
                    //已经是https,不处理
                }
            } catch (Exception e) {

            }
        }
        return newStr;
    }

    /**
     * 将链接转为http://
     * @param oldStr old Str
     */
    private static String str2Http(String oldStr) {
        String newStr = oldStr;
        if (newStr != null && !newStr.isEmpty()) {
            try {
                Uri uri = Uri.parse(newStr);
                String scheme = uri.getScheme();
                if (scheme == null) {
                    // 不是http 和 https的会直接返回null 的增加https协议
                    newStr = "http://" + newStr;
                } else if ("https".equals(scheme)) {
                    //http 转 https
                    newStr = newStr.replace("https://", "http://");
                } else if ("http".equals(scheme)) {
                    //已经是http,不处理
                }
            } catch (Exception e) {

            }
        }
        return newStr;
    }

    /**
     * 去除XXT链接中的锚点
     * @param urlStr 链接字符串
     * @return 处理后的无锚点的链接字符串
     */
    public static String removeFragmentOfXxtUrlStr(String urlStr) {
        String resultStr = urlStr;

        try {
            if (isFCDomain(urlStr)) {
                URL url = new URL(urlStr);
                if (!isEmpty(url.getRef())) {
                    resultStr = urlStr.replace(StringUtil.connectStrings("#", url.getRef()), "");
                }
            }
        } catch (Exception e) {

        }

        return resultStr;
    }

    public static String replaceDomain(String domain,String url){
        String url_bak = "";
        if(url.indexOf("//") != -1 ){
            String[] splitTemp = url.split("//");
            url_bak = splitTemp[0]+"//";

            url_bak = url_bak + domain;

            if(splitTemp.length >=1 && splitTemp[1].indexOf("/") != -1){
                String[] urlTemp2 = splitTemp[1].split("/");
                if(urlTemp2.length > 1){
                    for(int i = 1;i < urlTemp2.length; i++){
                        url_bak = url_bak +"/"+urlTemp2[i];
                    }
                }
            }else{
            }
        }
        return url_bak;
    }

    public static boolean isHyperLink(String url) {

        Pattern pattern = Pattern.compile("^[A-Za-z]+:/{0,3}[0-9.\\-A-Za-z]+(?::[\\d]+)?\\.[0-9\\-A-Za-z]+.*");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    public static boolean isUrlValid(String urlStr) {
        String[] schemas = {"http", "https"};
        UrlValidator urlValidator = new UrlValidator(schemas);
        return urlValidator.isValid(urlStr);
    }

}
