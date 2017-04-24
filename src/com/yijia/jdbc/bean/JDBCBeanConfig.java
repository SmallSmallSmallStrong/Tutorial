package com.yijia.jdbc.bean;

import com.yijia.util.Tool;

public class JDBCBeanConfig {

    private static JDBCBean jdbcBean;

    private JDBCBeanConfig() {

    }

    public static JDBCBean getJDBCBean() {

        if (jdbcBean == null) {
            synchronized(JDBCBeanConfig.class){
                if(jdbcBean == null){
                    init();
                }
            }
        }

        return jdbcBean;
    }

    private static JDBCBean init() {

        jdbcBean = new JDBCBean();
        String dirPath = JDBCBeanConfig.class.getResource("/").getPath();
        Tool.initProperties(dirPath + "jdbc.properties", jdbcBean);

        return jdbcBean;
    }
}
