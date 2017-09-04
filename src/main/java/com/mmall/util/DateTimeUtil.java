package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yao on 2017/8/21.
 */
public class DateTimeUtil {
    private static Logger logger = LoggerFactory.getLogger(DateTimeUtil.class);
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

    public static Date strToDateByDefaultFormat(String dateStr) throws ParseException {
        if(StringUtils.isBlank(dateStr)){
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
        Date date  = simpleDateFormat.parse(dateStr);
        return date;
    }
}
