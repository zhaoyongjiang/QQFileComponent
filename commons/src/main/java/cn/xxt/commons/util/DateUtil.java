package cn.xxt.commons.util;

import android.annotation.SuppressLint;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by xxthxn on 16/3/9.
 */
public final class DateUtil {
    public static final String DATE_FORMAT_STRING_YMDHMS = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT_STRING_YMDHM = "yyyy-MM-dd HH:mm";
    public static final String DATE_FORMAT_STRING_YMD = "yyyy-MM-dd";
    public static final String DATE_FORMAT_STRING_HM = "HH:mm";

    public static final String DATE_FORMAT_STRING_CN_YMD = "yyyy年MM月dd日";
    public static final String DATE_FORMAT_STRING_MD = "MM月dd日";
    public static final String DATE_FORMAT_STRING_MM = "h点";


    public static final String TIME_FORMAT_HMS = "hms";
    public static final String TIME_FORMAT_MS = "ms";
    public static final String TIME_FORMAT_M = "m";
    public static final String TIME_FORMAT_M_S = "m_s";

    /**
     * 将字符串形式的日期转换成日期格式
     * @param dateFormat
     * @param dateStr
     * @return null：转化失败
     */
    public static Date strDateToDate(String dateFormat, String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat(dateFormat, Locale.CHINA);
        Date date = null;
        try {
            date = format.parse(dateStr);
        } catch (Exception e) {
            Log.e("DateUtil", "Date parse failed", e);
        }
        return date;
    }

    /**
     * 获取当前时间，并且以指定的字符串返回
     * @param dateFormatStr
     * @return 当前时间
     */
    public static String getCurrentTime(String dateFormatStr) {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatStr, Locale.CHINA);
        return dateFormat.format(now);
    }

    /**
     * 将毫秒值转化为hh:mm:ss的格式字符串
     * @param milliseconds
     * @return
     */
    public static String convertTime(int milliseconds){
        // 获取总秒数及总分钟数
        int totalSecs = milliseconds / 1000;
        int taotalMins = totalSecs / 60;
        // 获取秒、分、时
        int secs = (totalSecs % 60);
        int mins = (taotalMins % 60);
        int hours = (taotalMins / 60);

        return String.format("%02d:%02d:%02d", hours, mins, secs);
    }

    /**
     * 将秒转化为分：秒
     * @param seconds
     * @return
     */
    public static String sec2MmSs(int seconds) {
        // 获取总秒数及总分钟数
        int totalSecs = seconds;
        int taotalMins = totalSecs / 60;
        // 获取秒、分、时
        int secs = (totalSecs % 60);
        int mins = (taotalMins % 60);

        return String.format("%02d:%02d", mins, secs);
    }

    /**
     * 将秒转换成冒号分隔的形式
     *
     * @version 1.0
     * @author tzhk 2012-4-26 11:45
     * @param  time（1800）
     * @return "01:30:00"
     */
    public static String formatHourMinSec(int time)	{
        String ret = "";
        try	{
            String strSec = String.valueOf(time % 60);
            String retSec = (strSec.length() == 1 ? "0" + strSec : strSec);
            ret = retSec;
            int remain = time/60;

            if (remain > 0) {
                String strMin = String.valueOf(remain%60);
                String retMin = (strMin.length() == 1 ? "0" + strMin : strMin);
                ret = retMin + ":" + ret;

                remain = remain/60;
                if (remain > 0) {
                    String strHour = String.valueOf(remain%60);
                    String retHour = (strHour.length() == 1 ? "0" + strHour : strSec);
                    ret = retHour + ":" + ret;
                }
            } else {
                ret = "00:" + ret;
            }
        } catch(Exception e) {
            ret =  "";
        }
        return ret;
    }

    /**
     * @version 1.0
     * @author tzhk 2012-4-26 11:45
     * @param time（1800）
     * @param format 要转换的格式
     * @return "01时30分00"
     */
    public static String formatHourMinSecWithUnit(int time, String format)
    {
        String timeStr = "";
        int second;
        int minute;
        int hour;

        try {
            second = time%60;
            minute = (time/60)%60;
            hour = ((time/60)/60)%60;

            if(format.equals(TIME_FORMAT_MS)){
                int minutes = hour*60+minute;
                if(minutes>0){
                    timeStr = String.valueOf(minutes+"分");
                }
                if(second>0){
                    timeStr = timeStr+String.valueOf(second)+"秒";
                }
            } else if (format.equals(TIME_FORMAT_M)) {
                int minutes = hour*60+minute;
                timeStr = String.valueOf(minutes+"分");
            } else if (format.equals(TIME_FORMAT_M_S)) {
                int minutes = hour*60 + minute;
                if (minutes > 0) {
                    timeStr = String.valueOf(minutes+"分");
                } else if(second>0){
                    timeStr = String.valueOf(second)+"秒";
                }
            } else {
                if(hour>0){
                    timeStr = String.valueOf(hour);
                }
                if(minute>0){
                    timeStr = timeStr + String.valueOf(minute+"分");
                }
                if(second>0){
                    timeStr = timeStr+String.valueOf(second)+"秒";
                }
            }

        } catch (Exception e) {

        }

        return timeStr;

    }

    /**
     * 毫秒数转换成日期时间
     *
     * @version 1.0
     * @author tzhk 2012-4-25 11:52
     * @param  time  （String） 1318045271000
     * @return "2012-4-25 11:52"
     */
    public static String getDateStrBytime(String time)
    {
        String ret = "";
        try {
            Date date=new Date();
            date.setTime(Long.parseLong(time));
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd hh:mm");
            ret = sdf.format(date);
        } catch(Exception e) {
            ret =  "";
        }
        return ret;
    }

    /**
     * 毫秒数转换成日期时间
     *
     * @version 1.0
     * @author tzhk 2012-4-25 11:52
     * @param  time  （String） 1318045271000
     * @return "2012年4月25日 11:52"
     */
    public static String getDateStrCnBytime(String time)
    {
        String ret = "";
        try
        {
            Date date=new Date();
            date.setTime(Long.parseLong(time));
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy年MM月dd日 hh:mm");
            ret = sdf.format(date);
        }
        catch(Exception e)
        {
            ret =  "";
        }
        return ret;
    }

    /**
     * 毫秒数转换成日期时间
     *
     * @version 1.0
     * @author tzhk 2012-4-25 11:52
     * @param  time  （String） 1318045271000
     * @param dateFormat 转换成的日期格式
     * @return "2012-4-25 11:52"
     */
    public static String getDateStrBytime(String time, String dateFormat)
    {
        String ret = "";
        try
        {
            Date date=new Date();
            date.setTime(Long.parseLong(time));
            SimpleDateFormat sdf=new SimpleDateFormat(dateFormat);
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            ret = sdf.format(date);
        }
        catch(Exception e)
        {
            ret =  "";
        }
        return ret;
    }

    /**
     * 获取星期几
     *
     * @version 1.0
     * @author tzhk 2012-5-23 10:06
     * @param  day "2012-5-23"
     * @return "星期三"
     */
    public static String getWeekDay(String day)
    {
        String ret = "";
        try
        {
            DateFormat sdf=new SimpleDateFormat(DATE_FORMAT_STRING_YMD);
            Date date = sdf.parse(day);
            DateFormat weekSdf =new SimpleDateFormat("EEEE");
            ret= weekSdf.format(date);
        }
        catch(Exception e)
        {
            ret =  "";
        }
        return ret;
    }

    /**
     * 获取周几
     * @param day
     * @return 周三
     */
    public static String getWeekDayZhou(String day)
    {
        String ret = "";
        try
        {
            DateFormat sdf=new SimpleDateFormat(DATE_FORMAT_STRING_YMD);
            Date date = sdf.parse(day);
            DateFormat weekSdf =new SimpleDateFormat("EEEE");
            ret= weekSdf.format(date);
            if("星期一".equals(ret)){
                ret = "周一";
            }else if("星期二".equals(ret)){
                ret = "周二";
            }else if("星期三".equals(ret)){
                ret = "周三";
            }else if("星期四".equals(ret)){
                ret = "周四";
            }else if("星期五".equals(ret)){
                ret = "周五";
            }else if("星期六".equals(ret)){
                ret = "周六";
            }else if("星期日".equals(ret)){
                ret = "周日";
            }
        } catch(Exception e) {
            ret =  "";
        }
        return ret;
    }

    /**
     * 获取时间格式化后的字符串值
     * @param date
     * @param dateFormat
     * @return
     */
    public static String format(Date date, String dateFormat) {
        if (dateFormat != null) {
            return new SimpleDateFormat(dateFormat).format(date);
        } else {
            return new SimpleDateFormat(DATE_FORMAT_STRING_YMDHM).format(date);
        }
    }

    /**
     * @author gjk 2009.11.10
     * 获取固定格式，的日期
     * @param formatType  格式 yyyyMMdd  MMdd  yyyyMMdd HH:mm:ss均可
     * @param delay   0表示的当天；1表示明天；-1表示昨天
     * @return
     */
    public static String getDayString(String formatType, int delay) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(formatType);
            Date d = new Date();
            long myTime = (d.getTime() / 1000) + delay * 24 * 60 * 60;
            d.setTime(myTime * 1000);
            return format.format(d);
        } catch (Exception e) {
            return "";
        }
    }
    /**
     * 获取指定日期间隔的日期
     * @param day 当前日期
     * @param formatType 格式 yyyyMMdd  MMdd  yyyyMMdd HH:mm:ss均可
     * @param delay 0表示的当天；1表示明天；-1表示昨天
     * @return
     */
    public static String getDayBeforeOrAfter(String day, String formatType, int delay) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(formatType);
            DateFormat sdf = new SimpleDateFormat(formatType);
            Date d = sdf.parse(day);
            long myTime = (d.getTime() / 1000) + delay * 24 * 60 * 60;
            d.setTime(myTime * 1000);
            return format.format(d);
        } catch (Exception e) {
            return "";
        }
    }
    /**
     * @author gjk 2009.11.10
     * 获取跟当前日期相差的日期模型
     * @param delay   0表示的当天；1表示明天；-1表示昨天
     * @return
     */
    public static Date getDayBeforeOrAfter(int delay) {
        try {
            Date d = new Date();
            long myTime = (d.getTime() / 1000) + delay * 24 * 60 * 60;
            d.setTime(myTime * 1000);
            return d;
        } catch (Exception e) {
            return new Date();
        }
    }
    /**
     * @author tzhk 2012-5-23
     * 比较传递的日期是否超过当前时间
     * @param day  传递的日期参数
     * @return  true 超过  false 没超过
     */
    public static boolean isDayExceedNow(String day) {
        try {
            DateFormat sdf=new SimpleDateFormat(DATE_FORMAT_STRING_YMD);
            Date date = sdf.parse(day);

            if (date.compareTo(new Date()) != 1) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }
    /**
     * @author tzhk 2012-5-23
     * 比较传递的日期是否超过当前时间
     * @param day  传递的日期参数
     * @return  true 超过  false 没超过
     */
    public static boolean isDayEqualsOrExceedToday(String day) {
        try {
            DateFormat sdf=new SimpleDateFormat(DATE_FORMAT_STRING_YMD);
            Date date = sdf.parse(day);

            String strToday = new SimpleDateFormat(DATE_FORMAT_STRING_YMD).format(new Date());

            Date today = sdf.parse(strToday);

            if (date.compareTo(today) != -1) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * @author tzhk 2012-5-23
     * 比较传递的日期是否是以前的日期
     * @param ymdhmsDate  传递的日期参数-格式年月日时分秒
     * @return  true 是以前的日期  false 不是以前的日期
     */
    public static boolean isOldDay(String ymdhmsDate) {
        try {
            DateFormat dfYMDHMS = new SimpleDateFormat(DATE_FORMAT_STRING_YMDHMS, Locale.CHINA);
            DateFormat dfYMD = new SimpleDateFormat(DATE_FORMAT_STRING_YMD, Locale.CHINA);

            Date dateYMDHMS = dfYMDHMS.parse(ymdhmsDate);
            String dateYMDString = dfYMD.format(dateYMDHMS);
            Date dateYMDate = dfYMD.parse(dateYMDString);

            String strToday = dfYMD.format(new Date());
            Date today = dfYMD.parse(strToday);

            if (dateYMDate.compareTo(today) < 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * @author tzhk 2012-5-23
     * 比较传递的日期是否是以前的日期
     * @param date  传递的日期参数-格式年月日时分秒
     * @param dateFormat 时间格式
     * @return  true 是以前的日期  false 不是以前的日期
     */
    public static boolean isOldDay(String date, String dateFormat) {
        try {
            DateFormat df = new SimpleDateFormat(dateFormat, Locale.CHINA);
            DateFormat dfYMD = new SimpleDateFormat(DATE_FORMAT_STRING_YMD, Locale.CHINA);

            Date dateYMDHMS = df.parse(date);
            String dateYMDString = dfYMD.format(dateYMDHMS);
            Date dateYMDate = dfYMD.parse(dateYMDString);

            String strToday = dfYMD.format(new Date());
            Date today = dfYMD.parse(strToday);

            if (dateYMDate.compareTo(today) < 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 比较两个日期大小
     * @param date1
     * @param date2
     * @param dateFormat
     * @param moreDate
     * @return <0:date1<date2; 0:date1=date2; >0:date1>date2
     */
    public static int compareDate(String date1, String date2, String dateFormat,
                                      long moreDate) {
        try {
            DateFormat df = new SimpleDateFormat(dateFormat, Locale.CHINA);

            Date date1Formated = df.parse(date1);
            Date date2Formated = df.parse(date2);

            date2Formated.setTime(date2Formated.getTime()+moreDate);

            return date1Formated.compareTo(df.parse(df.format(date2Formated)));

        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 比较两个日期的大小
     * @param time1
     * @param time2
     * @param dateFormat
     * @return0  如果参数time1等于time2，返回0; 如果time1在time2之前，返回一个小于0的值 ;  如果time1在time2之后，返回一个大于0的值。
     */
    public static int compareDate(String time1, String time2, String dateFormat){
        DateFormat df = new SimpleDateFormat(dateFormat, Locale.CHINA);
        try {
            Date date1 = df.parse(time1);
            Date date2 = df.parse(time2);
            return date1.compareTo(date2);

        } catch (Exception e) {

        }

        return 0;

    }

    /**
     * 格式化时间，去除小时、分钟、秒
     * @param date
     * @return
     * @throws ParseException
     */
    public static Date getZeroHourDay(Date date) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.parse(simpleDateFormat.format(date));
    }

    /**
     * 获取距今日的时间提示，如明天、今天
     * @param date
     * @return
     * @throws ParseException
     */
    public static String getTodayText(Date date) throws ParseException{
        String result = "";

        Date nowDate = getZeroHourDay(new Date());

        long l = date.getTime() - nowDate.getTime();
        long oneDay = 1000*60*60* 24L;
        if(l >= oneDay && l < oneDay * 2){
            result = "明天";
        }else if(l >= 0 && l < oneDay){
            result = "今天";
        }else{
            result =  new SimpleDateFormat(DATE_FORMAT_STRING_MD).format(date);
        }
        return result;
    }

    /**
     * 获取距今日的时间提示，如明天、今天
     * @param dateStr
     * @param dateFormatStr
     * @return
     * @throws ParseException
     */
    public static String getDayFormatText(String dateStr, String dateFormatStr){
        String result = "";

        try {
            DateFormat dateFormat = new SimpleDateFormat(dateFormatStr, Locale.CHINA);
            Date date = dateFormat.parse(dateStr);

            Date nowDate = getZeroHourDay(new Date());

            long oneDay = 24*60*60*1000L;
            long days = (date.getTime() - nowDate.getTime()) ;

            if (days >= 0 && days<oneDay) {
                    result = "今天";
            } else if(days>=oneDay && days<2*oneDay) {
                result = "明天";
            } else if(days>=oneDay && days<3*oneDay) {
                result = "后天";
            } else if(days>-2*oneDay && days<=-1*oneDay) {
                result = "昨天";
            } else if(days>-3*oneDay && days<=-2*oneDay) {
                result = "前天";
            } else if(date.getYear()==nowDate.getYear()) {
                result = new SimpleDateFormat(DATE_FORMAT_STRING_MD).format(date);
            }else{
                result =  new SimpleDateFormat(DATE_FORMAT_STRING_CN_YMD).format(date);
            }
        } catch (Exception e) {

        }
        return result;
    }

    /**
     * 转换时间为上下午格式
     * @param datepara
     * @return
     */
    public static String getTimeString(Date datepara) {
        String formatedDateString = "";
        Calendar cal = Calendar.getInstance();
        cal.setTime(datepara);
        int ampm = cal.get(Calendar.AM_PM);
        if (ampm == 0) {
            formatedDateString += "上午";
        } else if (ampm == 1) {
            formatedDateString += "下午";
        }
        formatedDateString += format(datepara, DATE_FORMAT_STRING_MM);;
        return formatedDateString;
    }

    /**
     * 按时间展示规则处理消息发生时间
     * @param date
     * @return
     */
    public static String dealCreateDate(String date) {
        String resultDate = "";
        try {
            String sendDateYMD = DateUtil.getDateStrBytime(date,
                    DateUtil.DATE_FORMAT_STRING_YMD);
            String sendDay = DateUtil.getDayFormatText(sendDateYMD,
                    DateUtil.DATE_FORMAT_STRING_YMD);
            String sendDateHM = DateUtil.getDateStrBytime(date,
                    DateUtil.DATE_FORMAT_STRING_HM);
            resultDate = StringUtil.connectStrings(sendDay, " ", sendDateHM);
        } catch (Exception e) {

        }
        return resultDate;
    }

    /**
     * 按时间展示规则处理消息发生时间
     * @param date 消息发生时间
     * @return 格式化输出:::  时间戳字符串转为年月日 周几 几点 类型
     */
//    @SuppressLint("SimpleDateFormat")
//    public static String dealCreateDate(String date) {
//        try {
//            String createDate = format(new Date(Long.valueOf(date)), DATE_FORMAT_STRING_YMDHMS);
//            return dealCreateDateWithDateStr(createDate);
//        } catch (Exception e) {
//
//        }
//        return "";
//    }

    /**
     * 按时间展示规则处理消息发生时间
     * @param date 消息发生时间
     * @return 格式化输出   :::   年月日 时分秒 转为 年月日 周几 几点 类型
     */
    @SuppressLint("SimpleDateFormat")
    public static String dealCreateDateWithDateStr(String date) {
        if (date == null || date.length() == 0) {
            return "";
        }
        // 今天 17:00
        if (!isOldDay(date)) {
            return "今天" + " " + date.substring(11, 16);
        } else if (compareDate(
                new SimpleDateFormat("yyyy-MM-dd").format(new Date()),
                date, "yyyy-MM-dd", 24 * 60 * 60 * 1000)==0) {
            // 昨天17:00
            return "昨天" + " " + date.substring(11, 16);
        } else if (compareDate(
                new SimpleDateFormat("yyyy-MM-dd").format(new Date()),
                date, "yyyy-MM-dd", 7 * 24 * 60 * 60 * 1000)<0) {
            // 一周内 周日17:00
            String weekDay = getWeekDayZhou(date);
            return weekDay + " " + date.substring(11, 16);
        } else if (compareDate(
                new SimpleDateFormat("yyyy").format(new Date()), date, "yyyy", 0)==0) {
            // 早于一周的且是今年的 04-08 17:00
            return date.substring(5, 16);
        } else  {
            // 早于今年的跨年 2013-12-21 周三 12:26
            String weekDay = getWeekDayZhou(date);
            return date.substring(0, 10) + " " + weekDay + " "
                    + date.substring(11, 16);
        }
    }

    /**
     * 按时间展示规则处理消息发生时间
     * @param date 消息发生时间
     * @return 格式化输出
     */
    @SuppressLint("SimpleDateFormat")
    public static String dealCreateDateInShortType(String date) {
        try {
            String createDate = format(new Date(Long.valueOf(date)), DATE_FORMAT_STRING_YMDHMS);
            dealCreateDateWithDateStrInShortType(createDate);
        } catch (Exception e) {

        }
        return "";
    }

    /**
     * 按时间展示规则处理消息发生时间
     * @param date 消息发生时间
     * @return 格式化输出
     */
    @SuppressLint("SimpleDateFormat")
    public static String dealCreateDateWithDateStrInShortType(String date) {
        if (date == null || date.length() == 0) {
            return "";
        }
        // 今天 17:00
        if (!isOldDay(date)) {
            return "今天" + " " + date.substring(11, 16);
        } else if (compareDate(
                new SimpleDateFormat("yyyy-MM-dd").format(new Date()),
                date, "yyyy-MM-dd", 24 * 60 * 60 * 1000)==0) {
            // 昨天17:00
            return "昨天" + " " + date.substring(11, 16);
        } else if (compareDate(
                new SimpleDateFormat("yyyy-MM-dd").format(new Date()),
                date, "yyyy-MM-dd", 7 * 24 * 60 * 60 * 1000)<0) {
            // 一周内 周日17:00
            String weekDay = getWeekDayZhou(date);
            return weekDay + " " + date.substring(11, 16);
        } else if (compareDate(
                new SimpleDateFormat("yyyy").format(new Date()), date, "yyyy", 0)==0) {
            // 早于一周的且是今年的 04-08 17:00
            return date.substring(5, 16);
        } else  {
            // 早于今年的跨年 2013-12-21
            String weekDay = getWeekDayZhou(date);
            return date.substring(0, 10);
        }
    }

    /**
     * long-string转化
     * @param currentTime
     * @param formatType
     * @return
     */
    public static String longToString(long currentTime, String formatType) {
        // 根据long类型的毫秒数生命一个date类型的时间
        Date dateOld = new Date(currentTime);
        // date类型转成String
        String strTime = format(dateOld, formatType);
        return strTime;
    }

    /**
     * 按时间展示规则处理消息发生时间
     * @param time 消息发生时间
     * @return 格式化输出
     */
    @SuppressLint("SimpleDateFormat")
    public static String dealCreateDateWithDateStrInShortType(long time) {
        if (time == 0) {
           return "";
        }
        try {
            String date = longToString(time, DATE_FORMAT_STRING_YMDHMS);
            return dealCreateDateWithDateStrInShortType(date);
        } catch (Exception e) {
            return "";
        }

    }

    /**
     * 下一个月的现在
     * @return：str
     */
    public static String nextMonthStr(String dateFormat) {
        return format(nextMonthDate(), dateFormat);
    }

    /**
     * 下一个月的现在
     * @return：date
     */
    public static Date nextMonthDate() {
        //得到一个Calendar的实例
        Calendar ca = Calendar.getInstance();
        //设置时间为当前时间
        ca.setTime(new Date());
        //年份减1
        ca.add(Calendar.MONTH, +1);
        //结果
        Date nextMonthDate = ca.getTime();
        return nextMonthDate;
    }

    public static boolean isSameDay(long stamp1, long stamp2) {
        Date date = new Date(stamp1);
        Date date2 = new Date(stamp2);
        Calendar calendar = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar.setTime(date);
        int y1 = calendar.get(Calendar.YEAR);
        int m1 = calendar.get(Calendar.MONTH);
        int d1 = calendar.get(Calendar.DAY_OF_YEAR);
        calendar2.setTime(date2);
        int y2 = calendar2.get(Calendar.YEAR);
        int m2 = calendar2.get(Calendar.MONTH);
        int d2 = calendar2.get(Calendar.DAY_OF_YEAR);
        if (y1 == y2 && m1 == m2 && d1 == d2) {
            return true;
        } else {
            return false;
        }
    }

    public static String date2TimeStamp(String date, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return String.valueOf(sdf.parse(date).getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
