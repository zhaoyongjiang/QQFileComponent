package cn.xxt.file.util;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by zyj on 2017/8/23.
 */

public class DateUtil {
    public static long timeNodeYearAgo(int year) {
        //得到一个Calendar的实例
        Calendar ca = Calendar.getInstance();
        //设置时间为当前时间
        ca.setTime(new Date());
        //年份减
        ca.add(Calendar.YEAR, -year);
        //结果
        long timeInMillis = ca.getTimeInMillis();
        return timeInMillis;
    }

    public static long timeNodeMonthAgo(int month) {
        //得到一个Calendar的实例
        Calendar ca = Calendar.getInstance();
        //设置时间为当前时间
        ca.setTime(new Date());
        //月份减
        ca.add(Calendar.MONTH, -month);
        //结果
        long timeInMillis = ca.getTimeInMillis();
        return timeInMillis;
    }

    public static long timeNodeDayAgo(int day) {
        //得到一个Calendar的实例
        Calendar ca = Calendar.getInstance();
        //设置时间为当前时间
        ca.setTime(new Date());
        //天减
        ca.add(Calendar.DAY_OF_MONTH, -day);
        //结果
        long timeInMillis = ca.getTimeInMillis();
        return timeInMillis;
    }

    public static long timeNodeTodayFirst(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long timeNodeYesterdayFirst() {
        Calendar calendar = Calendar.getInstance();
        clearCalendar(calendar, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return calendar.getTimeInMillis();
    }

    private static void clearCalendar(Calendar c, int... fields) {
        for (int f : fields) {
            c.set(f, 0);
        }
    }

    public static String sec2MmSs(int seconds) {
        // 获取总秒数及总分钟数
        int totalSecs = seconds;
        int taotalMins = totalSecs / 60;
        // 获取秒、分、时
        int secs = (totalSecs % 60);
        int mins = (taotalMins % 60);

        return String.format("%02d:%02d", mins, secs);
    }
}
