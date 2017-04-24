package com.yijia.elasticsearch.bean;

import com.yijia.util.Tool;

public class ESBean {

    /** 分片数 */
    private String numberOfShards;
    /** 每片的备份数 */
    private String numberOfReplicas;
    /** 访问路径 */
    private String host;
    /** 访问端口 */
    private String port;
    /** 节点名称 */
    private String clusterName;
    private String indexName;
    /** 收费站名称 */
    private String station;
    /** 收费站英文名称 */
    private String stationtoen;
    /** 自动同步间隔(毫秒) */
    private String synchInterval;
    /** 每次同步数据，单次分页查询最大量 */
    private String synchNum;
    /** 每天开始同步任务后的同步次数 */
    private String synchTime;
    /** 每天开始同步任务开启的时间（小时） */
    private String synchHour;
    /** 每天开始同步任务开启的时间 （分钟）*/
    private String synchMinute;
    /** 每天开始同步任务开启的时间 （秒）*/
    private String synchSecond;
    /** 进行同步的表名，完全同步完第一个表后才会同步第二个，推荐只使用一个表名，然后进行多个项目配置(*代表全部，多个表用前后逗号区分，例如：,z_user,z_role,) */
    private String synchTableName;
    /** 每一个同步表的排序和筛选字段 */
    private String synchTableOrderFieldName;
    /**最大连接数*/
    private String maxPoolSize;
    /**最小连接数*/
    private String minPoolSize;
    /**初始连接数*/
    private String initialPoolSize;
    /**空闲多长时间  单位为分钟*/
    private String maxIdleTime;
    /**每次同步的时间间隔*/
    private int timeInterval;
    
    

    /** 分片数 */
    public String getNumberOfShards() {
        return numberOfShards;
    }

    /** 分片数 */
    public void setNumberOfShards(String numberOfShards) {
        this.numberOfShards = numberOfShards;
    }

    /** 每片的备份数 */
    public String getNumberOfReplicas() {
        return numberOfReplicas;
    }

    /** 每片的备份数 */
    public void setNumberOfReplicas(String numberOfReplicas) {
        this.numberOfReplicas = numberOfReplicas;
    }
    
    /** 访问路径 */
    public String getHost() {
        return host;
    }

    /** 访问路径 */
    public void setHost(String host) {
        this.host = host;
    }

    /** 访问端口 */
    public String getPort() {
        return port;
    }

    /** 访问端口 */
    public void setPort(String port) {
        this.port = port;
    }

    /** 节点名称 */
    public String getClusterName() {
        return clusterName;
    }

    /** 节点名称 */
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    /** 收费站名称 */
    public String getStation() {
        return station;
    }

    /** 收费站名称 */
    public void setStation(String station) {
        this.station = station;
    }

    /** 收费站英文名称 */
    public String getStationtoen() {
        return stationtoen;
    }

    /** 收费站英文名称 */
    public void setStationtoen(String stationtoen) {
        this.stationtoen = stationtoen;
    }

    /** 自动同步间隔(毫秒) */
    public String getSynchInterval() {
        return synchInterval;
    }

    /** 自动同步间隔(毫秒) */
    public void setSynchInterval(String synchInterval) {
        this.synchInterval = synchInterval;
    }

    /** 每次同步数据，单次分页查询最大量 */
    public String getSynchNum() {
        return synchNum;
    }

    /** 每次同步数据，单次分页查询最大量 */
    public void setSynchNum(String synchNum) {
        this.synchNum = synchNum;
    }

    /** 每天开始同步任务后的同步次数 */
    public String getSynchTime() {
        return synchTime;
    }

    /** 每天开始同步任务后的同步次数 */
    public void setSynchTime(String synchTime) {
        this.synchTime = synchTime;
    }

    /** 每天开始同步任务开启的时间（小时） */
    public String getSynchHour() {
        return synchHour;
    }

    /** 每天开始同步任务开启的时间（小时） */
    public void setSynchHour(String synchHour) {
        this.synchHour = synchHour;
    }

    /** 每天开始同步任务开启的时间（分钟） */
    public String getSynchMinute() {
        return synchMinute;
    }

    /** 每天开始同步任务开启的时间（分钟） */
    public void setSynchMinute(String synchMinute) {
        this.synchMinute = synchMinute;
    }

    /** 每天开始同步任务开启的时间（秒） */
    public String getSynchSecond() {
        return synchSecond;
    }

    /** 每天开始同步任务开启的时间（秒） */
    public void setSynchSecond(String synchSecond) {
        this.synchSecond = synchSecond;
    }

    /** 进行同步的表名，完全同步完第一个表后才会同步第二个，推荐只使用一个表名，然后进行多个项目配置(*代表全部，多个表用前后逗号区分，例如：,z_user,z_role,) */
    public String getSynchTableName() {
        return synchTableName;
    }

    /** 进行同步的表名，完全同步完第一个表后才会同步第二个，推荐只使用一个表名，然后进行多个项目配置(*代表全部，多个表用前后逗号区分，例如：,z_user,z_role,) */
    public void setSynchTableName(String synchTableName) {
        this.synchTableName = synchTableName;
    }

    /** 每一个同步表的排序和筛选字段 */
    public String getSynchTableOrderFieldName() {
        return synchTableOrderFieldName;
    }

    /** 每一个同步表的排序和筛选字段 */
    public void setSynchTableOrderFieldName(String synchTableOrderFieldName) {
        this.synchTableOrderFieldName = synchTableOrderFieldName;
    }

    /**最大连接数*/
    public int getMaxPoolSize() {
        return Tool.sToI(maxPoolSize, 20);
    }

    /**最大连接数*/
    public void setMaxPoolSize(String maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    /**最小连接数*/
    public int getMinPoolSize() {
        return Tool.sToI(minPoolSize, 5);
    }

    /**最小连接数*/
    public void setMinPoolSize(String minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    /**初始连接数*/
    public int getInitialPoolSize() {
        return Tool.sToI(initialPoolSize, 5);
    }

    /**初始连接数*/
    public void setInitialPoolSize(String initialPoolSize) {
        this.initialPoolSize = initialPoolSize;
    }

    /**空闲多长时间  单位为分钟*/
    public int getMaxIdleTime() {
        return Tool.sToI(maxIdleTime, 4);
    }

    /**空闲多长时间  单位为分钟*/
    public void setMaxIdleTime(String maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }
    /**每次同步的时间间隔*/
    public int getTimeInterval() {
        return timeInterval;
    }
    /**每次同步的时间间隔*/
    public void setTimeInterval(int timeInterval) {
        this.timeInterval = timeInterval;
    }
    
}
