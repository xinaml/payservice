package com.aml.payservice.utils;

import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author liguiqin
 * @date 2020/9/4
 */
public class DateUtil {

    public static Date getMaxDate(){
        return DateUtil.parseDateTime("2099-12-31 23:59:59");
    }


    public static String getExpireTime(int minutes) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date now = new Date();
        Date afterDate = new Date(now.getTime() + (minutes * 1000));
        return sdf.format(afterDate);
    }

    public static String formatDateDay(Date date) {
        return formatDate(date, "yyyy-MM-dd");
    }

    public static String formatDateTime(Date date) {
        return formatDate(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static String formatDate(Date date, String formatStr) {
        return new SimpleDateFormat(formatStr).format(date);
    }

    public static Date parseDateTime(String timeString) {
        try {
            return DateUtils.parseDate(timeString,
                    new String[]{"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-M-d H:m:s",
                            "yyyy-MM-dd H:m:s", "yyyy-M-d HH:mm:ss", "yyyyMMddHHmmss"});
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date parseDate(String dateString) {
        try {
            return DateUtils.parseDate(dateString,
                    new String[]{"yyyy-MM-dd", "yyyy-MM", "yyyy-M-d", "yyyy-MM-d", "yyyy-M-dd"});
        } catch (ParseException e) {
            return null;
        }
    }


}
