package com.yijia.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;

public class Tool { 
    
    public final static String ENCODING = "UTF-8";
    public final static String FORMAT = "yyyy-MM-dd HH:mm:ss";
    public final static String FORMAT_YEAR = "yyyy";
    public final static String FORMAT_MONTH = "yyyy-MM";
    public final static String FORMAT_DAY = "yyyy-MM-dd";
    private static Calendar cal = Calendar.getInstance();
    
    /**
     * 判断传入的字符串的第一个字母，如果是大写转为小写，如果不是返回原字符串。
     * @param code
     * @return
     */
    public static String indexCodeLowerCase(String code){
        char[] chars = new char[1];  
        chars[0] = code.charAt(0);
        String str = code;
        String temp = new String(chars);  
        if(chars[0] >= 'A'  &&  chars[0] <= 'Z'){  
           str = code.replaceFirst(temp, temp.toLowerCase());  
        }
        return str;
    }
    
    /**
     * 初始化properties文件
     * @param propertiesPath-文件路径
     * @param module-反射对象
     */
    public static void initProperties(String propertiesPath, Object module){
        
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(propertiesPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Enumeration<?> enumeration = properties.propertyNames();// 得到配置文件的名字

        if (enumeration.hasMoreElements()) {

            Class<? extends Object> clazz = module.getClass();
            for (Method method : clazz.getMethods()) {
                if (method.getName().indexOf("set") == 0) {
                    String temp = method.getName().substring(3);
                    String key = Tool.indexCodeLowerCase(temp);
                    String value = properties.getProperty(key);
                    Method setMothod = null;
                    try {
                        setMothod = clazz.getMethod("set" + temp, String.class);
                        setMothod.invoke(module, value);
                    } catch (Exception e) {
                    }
                }
            }
        }
        
    }

    /**
     * 将字符串表示的整型数据转化为int型。
     * 
     * @param s
     *            描述整型数据的字符串
     * @return 返回转换好的int，如果转换时出错则返回0。
     */
    public static int sToI(String s) {
        return sToI(s, 0);
    }

    /**
     * 将字符串表示的整型数据转化为int型。
     * 
     * @param s
     *            描述整型数据的字符串
     * @param defaultValue
     *            转换失败时默认返回的值。
     * @return 返回转换好的int，如果转换时出错则返回defaultValue。
     */
    public static int sToI(String s, int defaultValue) {
        int i = defaultValue;
        try {
            i = Integer.parseInt(s);
        } catch (Exception e) {
        }
        return i;
    }

    /**
     * 将字符串表示的整型数据转化为long型。
     * 
     * @param s
     *            描述整型数据的字符串
     * @param defaultValue
     *            转换失败时默认返回的值。
     * @return 返回转换好的int，如果转换时出错则返回defaultValue。
     */
    public static long sToL(String s, long defaultValue) {
        long i = defaultValue;
        try {
            i = Long.parseLong(s);
        } catch (Exception e) {
        }
        return i;
    }
    
    /**
     * 将Long转为String
     * <pre>
     * @param l 如果转换失败，返回""
     * </pre>
     * @return
     */
    public static String L2S(Long l){
        return L2S(l, "");
    }
    
    /**
     * 将Long转为String
     * <pre>
     * @param l
     * @param defaultStr--如果转换失败，返回此值
     * </pre>
     * @return
     */
    public static String L2S(Long l, String defaultStr){
        String str = null;
        try {
            str = l.toString();
        } catch (Exception e) {
            if(defaultStr == null){
                str = "";
            }else{
                str = defaultStr;
            }
//          e.printStackTrace();
        }
        return str;
    }
    
    /**
     * 将Integer转为String
     * <pre>
     * @param i 如果转换失败，返回""
     * </pre>
     * @return
     */
    public static String I2S(Integer i){
        return I2S(i, "");
    }
    
    /**
     * 将Integer转为String
     * <pre>
     * @param i
     * @param defaultStr--如果转换失败，返回此值
     * </pre>
     * @return
     */
    public static String I2S(Integer i, String defaultStr){
        String str = null;
        try {
            str = i.toString();
        } catch (Exception e) {
            if(defaultStr == null){
                str = "";
            }else{
                str = defaultStr;
            }
//          e.printStackTrace();
        }
        return str;
    }

    /**
     * 模糊字符串<BR>
     * 用于sql模糊查询<BR>
     * 将字符串中的空格替换为“%”<BR>
     * 字符串两头添加“%” <BR>
     * 
     * @param strSource
     *            需要模糊的字符串
     * @return 模糊后的字符串
     */
    public static String fuzzy(String strSource) {
        String strResult = null;
        if (strSource == null || strSource.trim().length() == 0) {
            strResult = "%";
        } else {
            strResult = "%" + strSource.replace(' ', '%') + "%";
            strResult = strResult.replaceAll("%+", "%");
        }
        return strResult;
    }
    
    /**
     * 对时间time添加addYear年
     * @param time
     * @param addYear
     * @return
     */
    public static Date addYear(Date time, int addYear){
        cal.setTime(time);
        cal.add(Calendar.YEAR, addYear);
        return cal.getTime();
    }
    
    /**
     * 对时间time添加addMonth月
     * @param time
     * @param addMonth
     * @return
     */
    public static Date addMonth(Date time, int addMonth){
        cal.setTime(time);
        cal.add(Calendar.MONTH, addMonth);
        return cal.getTime();
    }
    
    /**
     * 对时间time添加addDay天
     * @param time
     * @param addDay
     * @return
     */
    public static Date addDay(Date time, int addDay){
        cal.setTime(time);
        cal.add(Calendar.DAY_OF_YEAR, addDay);
        return cal.getTime();
    }
    
    /**
     * 对时间time添加addHour小时
     * @param time
     * @param addHour
     * @return
     */
    public static Date addHour(Date time, int addHour){
        cal.setTime(time);
        cal.add(Calendar.HOUR_OF_DAY, addHour);
        return cal.getTime();
    }
    
    /**
     * 对时间time添加addMinute分钟
     * @param time
     * @param addMinute
     * @return
     */
    public static Date addMinute(Date time, int addMinute){
        cal.setTime(time);
        cal.add(Calendar.MINUTE, addMinute);
        return cal.getTime();
    }
    
    /**
     * 对时间time添加addSecond秒
     * @param time
     * @param addSecond
     * @return
     */
    public static Date addSecond(Date time, int addSecond){
        cal.setTime(time);
        cal.add(Calendar.SECOND, addSecond);
        return cal.getTime();
    }

    /**
     * 将日期（date）转换为给定模式（format）的字符串。<BR>
     * 如果给定日期为null返回默认字符串（defaultV）。<BR>
     * 给定模式为null或为空字符串时使用“yyyy年MM月dd日 HH:mm:ss”模式。<BR>
     * 
     * @param date
     *            需要转换的日期型对象。为java.util.Date类或其子类的对象。
     * @param format
     *            转换的模式，默认为yyyy年MM月dd日 HH:mm:ss
     * @param defaultV
     *            默认字符串，给定日期为null返回。
     * @return 返回格式化好的字符串，如果给定日期为null返回默认字符串。
     */
    public static String formatTime(java.util.Date date, String format,
            String defaultV) {
        if (date == null)
            return defaultV;
        if (format == null || format.trim().equals(""))
            format = "yyyy年MM月dd日 HH:mm:ss";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format);
        String dateS = sdf.format(date);
        return dateS;
    }

    /**
     * 将毫秒数表示的日期（time）转换为给定模式（format）的字符串。<BR>
     * 如果给定日期为0返回默认字符串（defaultV）。<BR>
     * 给定模式为null或为空字符串时使用“yyyy年MM月dd日 HH:mm:ss”模式。<BR>
     * 
     * @param time
     *            需要转换的日期（毫秒数）。
     * @param format
     *            转换的模式，默认为yyyy年MM月dd日 HH:mm:ss
     * @param defaultV
     *            默认字符串，给定日期毫秒数为0返回。
     * @return 返回格式化好的字符串，如果给定日期为null返回默认字符串。
     */
    public static String formatTime(long time, String format, String defaultV) {
        if (time == 0L)
            return defaultV;
        java.util.Date date = new java.util.Date(time);
        return formatTime(date, format, defaultV);
    }

    /**
     * 将字符串表示的日期转换为日期的毫秒数。<BR>
     * 字符串必须按"yyyy-MM-dd HH:mm:ss"模式。<BR>
     * 转换失败时返回0。<BR>
     * 
     * @param timeS
     *            表示日期的字符串
     * @return 返回日期的毫秒数。转换出错时返回0。
     */
    public static long parseTime(String timeS) {
        return parseTime(timeS, "yyyy-MM-dd HH:mm:ss").getTime();
    }

    /**
     * 将字符串表示的日期根据给定模式（format）转换为日期的毫秒数。<BR>
     * 转换失败时返回 null 。<BR>
     * 
     * @param timeS
     *            表示日期的字符串
     * @param format
     *            表示转换的模式
     * @return 返回日期的毫秒数。转换出错时返回 null。
     */
    public static Date parseTime(String timeS, String format) {
        if (format == null || format.trim().equals(""))
            format = "yyyy-MM-dd HH:mm:ss";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format);
        Date date = null;
        try {
            date = sdf.parse(timeS);
        } catch (Exception ex) {
            // ex.printStackTrace();
            date = null;
        }
        return date;
    }
    
    /**
     * 生成随机数（取 min~max的随机数），如果都是null则取（1000~9999的随机数）
     * @param min
     * @param max
     * @return
     */
    public static Integer randomNum(Integer min, Integer max){
        int startNum = 1000;
        int endNum = 9999;
        if(min != null){
            startNum = min;
        }
        if(max != null){
            endNum = max;
        }
        if(startNum > endNum){
            startNum = 1000;
            endNum = 9999;
        }
        Random random = new Random();
        Integer s = random.nextInt(endNum) % (endNum - startNum + 1) + startNum;
        return s;
    }
    
    /**
     * 生成随机数（取 min~max的随机数），如果都是null则取（1000~9999的随机数）
     * @param min
     * @param max
     * @return
     */
    public static String randomStrNum(Integer min, Integer max){
        return randomNum(min, max) + "";
    }
    
    /**
     * 产生随机字符-英文字母和数字
     * @param length-长度
     * @return
     */
    public static String randomStr(int length) { //length表示生成字符串的长度 
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        return randomStr(base, length);
     }    
    
    /**
     * 产生随机字符-英文字母和数字
     * @param length-长度
     * @return
     */
    public static String randomStr(String base, int length) { //length表示生成字符串的长度  
        Random random = new Random();
        StringBuffer sb = new StringBuffer();     
        for (int i = 0; i < length; i++) {     
            int number = random.nextInt(base.length());     
            sb.append(base.charAt(number));     
        }     
        return sb.toString();     
    }

    /**
     * 生成随机时间 格式：yyyy-MM-dd
     * 
     * @param beginDate
     * @param endDate
     * @return
     */
    public  static Date randomDate(String beginDate, String endDate) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date start = format.parse(beginDate);// 构造开始日期
            Date end = format.parse(endDate);// 构造结束日期
            // getTime()表示返回自 1970 年 1 月 1 日 00:00:00 GMT 以来此 Date 对象表示的毫秒数。
            
            if (start.getTime() >= end.getTime()) {
                return null;
            }
            long date = random(start.getTime(), end.getTime());
            return new Date(date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static long random(long begin, long end) {
        long rtn = begin + (long) (Math.random() * (end - begin));
        // 如果返回的是开始时间和结束时间，则递归调用本函数查找随机值
        if (rtn == begin || rtn == end) {
            return random(begin, end);
        }
        return rtn;
    }  
    
    /**
     * 判断str是不是null，如果是null或者是空格返回true，否则返回false
     * @param str
     * @return
     */
    public static boolean isEmpty(String str){
        if(str == null || str.trim().length() <= 0){
            return true;
        }else{
            return false;
        }
    }
    
    /**
     * 判断objs数组是不是null，如果是null或者是空格返回true，否则返回false
     * 
     * @param objs
     * @return
     */
    public static boolean isEmpty(Object... objs) {
        if (objs == null || objs.length <= 0) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * 判断Collection容器是不是null，如果是null或者是空格返回true，否则返回false
     * 
     * @param collection
     * @return
     */
    public static boolean isEmpty(Collection<?> collection) {
        if (collection == null || collection.size() <= 0) {
            return true;
        } else {
            return false;
        }
    }
}
