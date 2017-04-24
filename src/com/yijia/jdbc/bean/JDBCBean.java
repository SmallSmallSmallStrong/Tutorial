package com.yijia.jdbc.bean;

public class JDBCBean {

    /** 数据库类型：mysql、db2、sqlServer */
    private String databaseType;
    /** 驱动 */
    private String driver;
    /** 链接地址 */
    private String serverip;
    private String username;
    private String password;



    /** 数据库类型：mysql、db2、sqlServer */
    public String getDatabaseType() {
        return databaseType;
    }

    /** 数据库类型：mysql、db2、sqlServer */
    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    /** 驱动 */
    public String getDriver() {
        return driver;
    }

    /** 驱动 */
    public void setDriver(String driver) {
        this.driver = driver;
    }

    /** 链接地址 */
    public String getServerip() {
        return serverip;
    }

    /** 链接地址 */
    public void setServerip(String serverip) {
        this.serverip = serverip;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
