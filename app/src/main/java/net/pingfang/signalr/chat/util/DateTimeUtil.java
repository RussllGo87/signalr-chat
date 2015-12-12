package net.pingfang.signalr.chat.util;

import android.content.Context;

import net.pingfang.signalr.chat.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by gongguopei87@gmail.com on 2015/12/12.
 */
public class DateTimeUtil {

    public static final int FLAG_PARSE_DATE_ERROR = -1;
    public static final int FLAG_BEFORE_YESTERDAY = 0x00;
    public static final int FLAG_YESTERDAY = 0x01;
    public static final int FLAG_TODAY = 0x02;

    /**
     * 处理服务器返回的时间
     *
     * @param datetime 时间字符串
     * @return
     */
    public static String convertServerTime(String datetime) {
        int index = datetime.indexOf('T');
        String date = datetime.substring(0, index);
        int dotIndex = datetime.indexOf('.');
        String time = datetime.substring((index + 1), dotIndex);

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(date);
        stringBuffer.append(" ");
        stringBuffer.append(time);
        return stringBuffer.toString();
    }

    public static String TimeConvertString() {
        Date now = new Date();
        return TimeConvertString(now);
    }

    public static String TimeConvertString(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(calendar.get(Calendar.YEAR));
        stringBuffer.append("-");
        int month = calendar.get(Calendar.MONTH);
        month = month + 1;
        if (month < 10) {
            stringBuffer.append("0");
        }
        stringBuffer.append(month);
        stringBuffer.append("-");
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        if (day < 10) {
            stringBuffer.append("0");
        }
        stringBuffer.append(day);
        stringBuffer.append(" ");

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour < 10) {
            stringBuffer.append("0");
        }
        stringBuffer.append(hour);
        stringBuffer.append(":");
        int minute = calendar.get(Calendar.MINUTE);
        if (minute < 10) {
            stringBuffer.append("0");
        }
        stringBuffer.append(minute);
        stringBuffer.append(":");
        int second = calendar.get(Calendar.SECOND);
        if (second < 10) {
            stringBuffer.append("0");
        }
        stringBuffer.append(second);

        return stringBuffer.toString();
    }

    public static int convertDatetimeFormat(String datetime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        try {
            Date date = dateFormat.parse(datetime);

            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            Date today = calendar.getTime();

            calendar.set(Calendar.DAY_OF_MONTH, -1);
            Date yesterday = calendar.getTime();

            if (date.before(yesterday)) {
                return FLAG_BEFORE_YESTERDAY;
            } else if (date.after(today)) {
                return FLAG_TODAY;
            } else {
                return FLAG_YESTERDAY;
            }
        } catch (ParseException e) {
            e.printStackTrace();

            return FLAG_PARSE_DATE_ERROR;
        }
    }

    public static String displayDateOrTime(Context context, String datetime, int flag) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        try {
            Date date = dateFormat.parse(datetime);
            switch (flag) {
                case FLAG_BEFORE_YESTERDAY:
                    return new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(date);
                case FLAG_YESTERDAY:
                    return context.getString(R.string.date_display_yesterday);
                case FLAG_TODAY:
                    return new SimpleDateFormat("HH:mm:ss", Locale.CHINA).format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return context.getString(R.string.date_display_error);
        }

        return datetime;
    }

    public static String displayDateAndTime(Context context, String datetime, int flag) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        try {
            Date date = dateFormat.parse(datetime);
            switch (flag) {
                case FLAG_BEFORE_YESTERDAY:
                    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(date);
                case FLAG_YESTERDAY:
                    return context.getString(R.string.date_display_yesterday) + " " + new SimpleDateFormat("HH:mm:ss", Locale.CHINA).format(date);
                case FLAG_TODAY:
                    return context.getString(R.string.date_display_today) + " " + new SimpleDateFormat("HH:mm:ss", Locale.CHINA).format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return context.getString(R.string.date_display_error);
        }

        return datetime;
    }
}
