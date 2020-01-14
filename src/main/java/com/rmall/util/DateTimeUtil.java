package com.rmall.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * @author 大神爱吃茶
 * 时间处理工具类
 * 使用joda-time（DateTimeFormat,DateTimeFormatter）
 * */
public class DateTimeUtil {
    public static final String STANDARD_FORMAT="yyyy-MM-dd HH:mm:ss";

    //将str转换成datetime
    public static Date strToDate(String dateTimeStr, String formartStr){
        //传入格式并封装
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(formartStr);
        //将传进来的字符串封装为一个DateTime对象
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        //返回date对象
        return dateTime.toDate();
    }

    //将datetime转换成str
    public static String dateToStr(Date date,String formatStr){
        if(date == null){
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(formatStr);
    }


    //将str转换成datetime（标准化）
    public static Date strToDate(String dateTimeStr){
        //传入格式并封装
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(STANDARD_FORMAT);
        //将传进来的字符串封装为一个DateTime对象
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        //返回date对象
        return dateTime.toDate();
    }

    //将datetime转换成str（标准化）
    public static String dateToStr(Date date){
        if(date == null){
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(STANDARD_FORMAT);
    }

}
