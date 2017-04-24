package com.yijia.jdbc.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.yijia.util.Tool;

public class JDBCUtil_DB2 extends JDBCUtil {

       private Logger logger = Logger.getLogger(JDBCUtil_DB2.class);

       /**
        * 获取数据库中所有表名
        * @throws SQLException 获取数据库中所有表名失败 可能数据库无法连接
        */
       public List<Object> getTableList() throws SQLException {
              List<Object> tableList = new ArrayList<Object>();
              ResultSet res = null;
              try {
                     Connection con = conn();
                     if (con != null) {
                            DatabaseMetaData dbMetaData = con.getMetaData();
                            String[] types = { "TABLE" };
                            res = dbMetaData.getTables(null, getSchema(), null, types/* 只要表就好了 */);
                            while (res.next()) {
                                   // 只要表名这一列
                                   tableList.add(res.getObject("TABLE_NAME"));
                            }
                            closeResultSet(res);
                     }else{
                         logger.error("Connection连接为空-getTableList");
                     }
                     closeConn();
              } catch (SQLException e) {
                     logger.error("获取数据库中所有表名失败。", e);
                     throw e;
              } 
              return tableList;
       }

       /**
        * 获取列信息
        * 
        * @param tableName
        * @return map->key:columnNameList（列名）、typeNameList（每列类型）;
        * @throws SQLException 获取该表列消息失败
        */
       public Map<String, List<String>> getColumn(String tableName) throws SQLException {
              // 字段名称list
              List<String> columnNameList = new ArrayList<String>();
              // 字段类型list
              List<String> typeNameList = new ArrayList<String>();
              Map<String, List<String>> columnMap = new HashMap<String, List<String>>();
              PreparedStatement stmt = null;
              ResultSet res = null;
              try {
                     long startTime = System.currentTimeMillis();
                     String sql = pageSQL(tableName, null, null, 1, 1);
                     Connection con = conn();
                     if (con != null) {
                            stmt = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                                          ResultSet.CONCUR_READ_ONLY);
                            res = stmt.executeQuery();
                            Integer fieldCount = res.getMetaData().getColumnCount();
                            for (int i = 1; i <= fieldCount; i++) {
                                   String columnName = res.getMetaData().getColumnName(i);
                                   columnNameList.add(columnName);
                                   String typeName = res.getMetaData().getColumnTypeName(i);
                                   typeNameList.add(typeName);
                            }
                            logger.info("获取（" + fieldCount + "）列耗时：" + (System.currentTimeMillis() - startTime)
                                          + " ms");
                            closeResStmt(stmt, res);
                     }else{
                         logger.error("Connection为空--getColumn");
                     }
                     closeConn();
              } catch (SQLException e) {
                     logger.error("获取数据库【" + tableName + "】列信息出错。", e);
                     throw e;
              } 
              columnMap.put("columnNameList", columnNameList);
              columnMap.put("typeNameList", typeNameList);
              return columnMap;
       }

       /**
        * 根据表名、分页、条件（and name = 'wang'）、排序sql、开始条数（包括本条），结束条数（不包括本条）生成DB2的分页语句
        * 
        * @param tableName
        * @param params
        * @param orderSQL
        * @param startNum
        * @param endNum
        * @return
        */
       public String pageSQL(String tableName, List<String> params, String orderSQL, long startNum, int synchNum) {
              String over = null;
              if (Tool.isEmpty(orderSQL)) {
                     over = "OVER()";
              } else {
                     over = "OVER(" + orderSQL + ")";
              }
              StringBuffer childSQL = new StringBuffer("select a.*, ROW_NUMBER() " + over + " as row_number " + "from "
                            + tableName + " as a where 1 = 1 ");
              if (!Tool.isEmpty(params)) {
                     for (String param : params) {
                            childSQL.append(param);
                     }
              }
              String sql = "select b.* from ( " + childSQL + " ) as b where " + "b.row_number >= " + startNum
                            + " and b.row_number < " + (startNum + synchNum);

              return sql;
       }
}
