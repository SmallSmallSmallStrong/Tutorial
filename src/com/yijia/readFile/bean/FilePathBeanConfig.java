package com.yijia.readFile.bean;

import com.yijia.util.Tool;

public class FilePathBeanConfig {

    private static FilePathBean bean;

    private FilePathBeanConfig() {

    }

    public static FilePathBean getBean() {

        if (bean == null) {
            synchronized(FilePathBeanConfig.class){
                if(bean == null){
                    init();
                }
            }
        }

        return bean;
    }

    private static FilePathBean init() {

        bean = new FilePathBean();
        String dirPath = FilePathBeanConfig.class.getResource("/").getPath();
        Tool.initProperties(dirPath + "filePath.properties", bean);

        return bean;
    }
    
}
