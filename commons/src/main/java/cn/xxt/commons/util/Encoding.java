package cn.xxt.commons.util;

/**
 * Created by Luke on 16/11/30.
 */

public class Encoding {
        public static int GB2312 = 0;

        public static int GBK = 1;

        public static int BIG5 = 2;

        public static int UTF8 = 3;

        public static int UNICODE = 4;

        public static int UNKNOWN = 5;

        public static int TOTALT = 6;

        public static final int SIMP = 0;
        public static final int TRAD = 1;

        public static String[] javaname;
        public static String[] nicename;
        public static String[] htmlname;

        public Encoding()
        {
            javaname = new String[TOTALT];
            nicename = new String[TOTALT];
            htmlname = new String[TOTALT];
            javaname[GB2312] = "GB2312";
            javaname[GBK] = "GBK";
            javaname[BIG5] = "BIG5";
            javaname[UTF8] = "UTF8";
            javaname[UNICODE] = "Unicode";
            javaname[UNKNOWN] = "UTF-8";

            htmlname[GB2312] = "GB2312";
            htmlname[GBK] = "GBK";
            htmlname[BIG5] = "BIG5";
            htmlname[UTF8] = "UTF-8";
            htmlname[UNICODE] = "UTF-16";
            htmlname[UNKNOWN] = "ISO8859-1";

            nicename[GB2312] = "GB-2312";
            nicename[GBK] = "GBK";
            nicename[BIG5] = "Big5";
            nicename[UTF8] = "UTF-8";
            nicename[UNICODE] = "Unicode";
            nicename[UNKNOWN] = "UTF-8";
        }

        public String toEncoding(int type) {
            return (javaname[type] + "," + nicename[type] + "," + htmlname[type])
                    .intern();
        }
}
