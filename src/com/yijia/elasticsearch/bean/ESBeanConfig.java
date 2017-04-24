package com.yijia.elasticsearch.bean;

import com.yijia.util.Tool;

public class ESBeanConfig {

    private static ESBean esBean;

    private ESBeanConfig() {

    }

    public static ESBean getESBean() {

        if (esBean == null) {
            synchronized(ESBeanConfig.class){
                if(esBean == null){
                    init();
                }
            }
        }

        return esBean;
    }

    private static ESBean init() {

        esBean = new ESBean();
        String dirPath = ESBeanConfig.class.getResource("/").getPath();
        Tool.initProperties(dirPath + "elasticsearch.properties", esBean);

        return esBean;
    }
    
}
