package com.yijia.jdbc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.druid.pool.DruidPooledConnection;

public class DbPoolConnection {
    
    private static Logger logger = Logger.getLogger(DbPoolConnection.class);

    private static DbPoolConnection dbPoolConnection = null;
    private static DruidDataSource dds = null;
    
    static {
        Properties properties = loadPropertyFile("druid.properties");
        try {
            dds = (DruidDataSource) DruidDataSourceFactory.createDataSource(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DbPoolConnection() {
    }

    public static synchronized DbPoolConnection getInstance() {
        if (null == dbPoolConnection) {
            dbPoolConnection = new DbPoolConnection();
        }
        return dbPoolConnection;
    }

    /**
     * 获取数据库连接
     * @return
     */
    public DruidPooledConnection getConnection() {
        try {
            return dds.getConnection();
        } catch (SQLException e) {
            logger.error("获取连接失败。", e);
        }
        return null;
    }

    public static Properties loadPropertyFile(String fullFile) {
        String webRootPath = null;
        if (null == fullFile || fullFile.equals(""))
            throw new IllegalArgumentException("Properties file path can not be null : " + fullFile);
        webRootPath = DbPoolConnection.class.getClassLoader().getResource("").getPath();
        webRootPath = new File(webRootPath).getParent();
        InputStream inputStream = null;
        Properties p = null;
        try {
            inputStream = new FileInputStream(new File(webRootPath + "/classes/" + fullFile));
            p = new Properties();
            p.load(inputStream);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Properties file not found: " + fullFile);
        } catch (IOException e) {
            throw new IllegalArgumentException("Properties file can not be loading: " + fullFile);
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return p;
    }
}
