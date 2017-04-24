package com.yijia.util;

import java.util.Collection;

public class ToolAdd {
    
    /**
     * 字符串转Double
     * 
     * @param str
     * @param defaultV 转换失败用这个值
     * @return
     */
    public static Double s2d(String str, Double defaultV){
        Double result = null;
        try {
            result = Double.parseDouble(str);
        } catch (NumberFormatException e) {
            result = defaultV;
        }
        return result;
    }

    /**
     * 字符串转Double
     * 
     * @param str
     * @param defaultV 默认是0
     * @return
     */
    public static Double s2d(String str){
        return s2d(str, 0d);
    }
    
    /**
     * 判断容器是不是null
     * 
     * @param collection
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(Collection collection){
        if(collection == null || collection.size() <= 0){
            return true;
        }else{
            return false;
        }
    }

}
