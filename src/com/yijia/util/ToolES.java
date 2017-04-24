package com.yijia.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ToolES {
    
    public static final String TIME_ZONE = "UTC";
    
    /**
     * 2007-10-23T17:15:44.000Z格式转date
     * 
     * @param timeZone 对应的字符串
     * @param UTC 时区
     * @param defaultV 默认值
     * @return
     */
    public static Date timeZone2Date(String timeZone, String UTC, Date defaultV){
        Date date = defaultV;
        if(Tool.isEmpty(timeZone)){
            return date;
        }
        timeZone = timeZone.replace("Z", " " + UTC);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
        try {
            date = sdf.parse(timeZone);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
    
    /**
     * 2007-10-23T17:15:44.000Z格式转date
     * 
     * @param timeZone 对应的字符串
     * @param UTC 时区
     * @param defaultV 默认 null
     * @return
     */
    public static Date timeZone2Date(String timeZone, String UTC){
        return timeZone2Date(timeZone, UTC, null);
    }
    
    /**
     * 2007-10-23T17:15:44.000Z格式转date
     * 
     * @param timeZone 对应的字符串
     * @param UTC 默认 “UTC”
     * @param defaultV 默认值
     * @return
     */
    public static Date timeZone2Date(String timeZone, Date defaultV){
        return timeZone2Date(timeZone, TIME_ZONE, defaultV);
    }
    
    /**
     * 2007-10-23T17:15:44.000Z格式转date
     * 
     * @param timeZone 对应的字符串
     * @param UTC 默认 “UTC”
     * @param defaultV 默认 null
     * @return
     */
    public static Date timeZone2Date(String timeZone){
        return timeZone2Date(timeZone, TIME_ZONE);
    }
    
    /**
     * 2007-10-23T17:15:44.000Z按指定格式转出字符串
     * 
     * @param timeZone 对应的字符串
     * @param format 默认为 yyyy-MM-dd HH:mm:ss
     * @param defaultV 默认为 null
     * @return
     */
    public static String timeZone2Str(String timeZone){
        return timeZone2Str(timeZone, "yyyy-MM-dd HH:mm:ss");
    }
    
    /**
     * 2007-10-23T17:15:44.000Z按指定格式转出字符串
     * 
     * @param timeZone 对应的字符串
     * @param format 格式化方式
     * @param defaultV 默认为 null
     * @return
     */
    public static String timeZone2Str(String timeZone, String format){
        return timeZone2Str(timeZone, format, null);
    }
    
    /**
     * 2007-10-23T17:15:44.000Z按指定格式转出字符串
     * 
     * @param timeZone 对应的字符串
     * @param format 格式化方式
     * @param defaultV 默认值
     * @return
     */
    public static String timeZone2Str(String timeZone, String format, String defaultV){
        return Tool.formatTime(timeZone2Date(timeZone), format, defaultV);
    }
    
    public static void main(String[] args) {
        String ts = "2007-10-23T17:15:44.000Z";
        System.out.println(ToolES.timeZone2Str(ts, "yyyy-MM-dd HH:mm:ss", ""));
    }

}
