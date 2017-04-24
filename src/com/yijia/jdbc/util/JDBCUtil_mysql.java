package com.yijia.jdbc.util;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class JDBCUtil_mysql extends JDBCUtil {
    
    private Logger logger = Logger.getLogger(JDBCUtil_mysql.class);

    /**
     * 获取数据库中所有表名
     */
    public List<Object> getTableList() {
        List<Object> tableList = new ArrayList<Object>();
        ResultSet res = null;
        try {
            DatabaseMetaData dbMetaData = conn().getMetaData();
            String[] types = { "TABLE" };
            res = dbMetaData.getTables(null, null, null, types/* 只要表就好了 */);
            while (res.next()) {
                // 只要表名这一列
                tableList.add(res.getObject("TABLE_NAME"));
            }
        } catch (SQLException e) {
            logger.error("获取表列表失败。", e);
        } finally {
            JDBCUtil_mysql.closeResultSet(res);
        }
        return tableList;
    }
    
    /**
     * 获取列信息
     * 
     * @param tableName
     * @return map->key:columnNameList（列名）、typeNameList（每列类型）;
     */
    public Map<String, List<String>> getColumn(String tableName){
        // 字段名称list
        List<String> columnNameList = new ArrayList<String>();
        // 字段类型list
        List<String> typeNameList = new ArrayList<String>();
        Map<String, List<String>> columnMap = new HashMap<String, List<String>>();
        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            String sql = "select * from " + tableName + " limit 0,1";
            stmt = conn().prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            res = stmt.executeQuery();
            Integer fieldCount = res.getMetaData().getColumnCount();
            for (int i = 1; i <= fieldCount; i++) {
                String columnName = res.getMetaData().getColumnName(i);
                columnNameList.add(columnName);
                String typeName = res.getMetaData().getColumnTypeName(i);
                typeNameList.add(typeName);
            }
        } catch (SQLException e) {
            logger.error("获取数据库【" + tableName + "】列信息出错。", e);
        } finally {
            JDBCUtil_mysql.closeResStmt(stmt, res);
        }
        columnMap.put("columnNameList", columnNameList);
        columnMap.put("typeNameList", typeNameList);
        return columnMap;
    }
}
