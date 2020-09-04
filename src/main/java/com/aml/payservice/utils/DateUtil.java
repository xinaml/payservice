package com.aml.payservice.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author liguiqin
 * @date 2020/9/4
 */
public class DateUtil {
    public static String getExpireTime(int minutes){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date now = new Date();
        Date afterDate = new Date(now .getTime() + (minutes*1000));
        return sdf.format(afterDate );
    }
}
