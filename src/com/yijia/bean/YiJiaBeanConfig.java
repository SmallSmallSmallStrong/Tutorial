package com.yijia.bean;

import com.yijia.util.Tool;

public class YiJiaBeanConfig {

    private static YiJiaBean yijiaBean;

    private YiJiaBeanConfig() {

    }

    public static YiJiaBean getYiJiaBean() {

        if (yijiaBean == null) {
            synchronized(YiJiaBeanConfig.class){
                if(yijiaBean == null){
                    init();
                }
            }
        }

        return yijiaBean;
    }

    private static YiJiaBean init() {

        yijiaBean = new YiJiaBean();
        String dirPath = YiJiaBeanConfig.class.getResource("/").getPath();
        Tool.initProperties(dirPath + "yijia.properties", yijiaBean);

        return yijiaBean;
    }
}
