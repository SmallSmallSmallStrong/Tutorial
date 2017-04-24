package com.yijia.jdbc;

/**
 * 同步服务的接口
 * @author zhqy
 *
 */
public interface ConnectServer {

    /**
     * 获取数据库表名 对每一个表同步数据
     * 
     * @param synchTableName 指定同步表名(null表示根据配置文件， *代表全部，多个表用前后逗号区分，例如：,z_user,z_role,)
     */
    public void synch();

    /**
     * 在es中创建索引插入数据（快速），找到最后一条的条数，从最后一条继续插入
     * 
     * @param tableName
     */
    public void creatESFast(String tableName);

    /**
     * 关闭数据库和ES连接
     */
    public void close();

}
