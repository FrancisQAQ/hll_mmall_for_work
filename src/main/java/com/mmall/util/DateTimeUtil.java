package com.mmall.util;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yao on 2017/8/21.
 */
public class DateTimeUtil {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String formatDate(Date date,String dateFormat){
        if(date == null){
            return StringUtils.EMPTY;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        String result = simpleDateFormat.format(date);
        return result;
    }

    public static String formatDateByDefaultFormat(Date date){
        if(date == null){
            return StringUtils.EMPTY;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
        String result = simpleDateFormat.format(date);
        return result;
    }
}
