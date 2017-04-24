package com.yijia.jdbc.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import com.yijia.util.Tool;

public class JDBCUtil {

       private static Logger logger = Logger.getLogger(JDBCUtil.class);

       private Connection    co     = null;

       /**
        * 获取数据库连接
        * 
        * @return
        * @throws SQLException
        */
       public Connection conn() throws SQLException {
              try {
                     if (co == null || co.isClosed()) {
                            co = DbPoolConnection.getInstance().getConnection();
                     }
              } catch (SQLException e) {
                     logger.error("获取链接失败。数据库无响应。", e);
                     throw new SQLException("获取链接失败，数据库无响应。");
              }
              return co;
       }

       /**
        * DB2、ORACLE数据库模式获取schema
        * 
        * @return
        */
       protected String getSchema() {
              String schema = null;
              try {
                     schema = conn().getMetaData().getUserName();
                     if ((schema == null) || (schema.length() == 0)) {
                            throw new Exception("DB2、ORACLE数据库模式不允许为空");
                     }
              } catch (SQLException e) {
                     logger.error("获取schema名称失败。", e);
              } catch (Exception e) {
                     logger.error(e);
              }
              return schema.toUpperCase().toString();
       }

       /**
        * 关闭PreparedStatement ResultSet
        * 
        * @param stmt
        * @param res
        */
       public static void closeResStmt(Statement stmt, ResultSet res) {
              closeResultSet(res);
              closeStatement(stmt);
              logger.info("关闭数据链接 res stmt ");
       }

       /**
        * 关闭 ResultSet
        * 
        * @param res
        */
       public static void closeResultSet(ResultSet res) {
              try {
                     if (res != null) {
                            res.close();
                     }
              } catch (SQLException e) {
                     logger.error("ResultSet关闭失败。", e);
              } finally {
                     res = null;
              }
       }

       /**
        * 关闭 Statement
        * 
        * @param stmt
        */
       public static void closeStatement(Statement stmt) {
              try {
                     if (stmt != null) {
                            stmt.close();
                     }
              } catch (SQLException e) {
                     logger.error("Statement关闭失败。", e);
              } finally {
                     stmt = null;
              }
       }

       /**
        * 关闭数据库连接
        */
       public void closeConn() {
              try {
                     if (this.co != null) {
                            this.co.close();
                     }
              } catch (SQLException e) {
                     logger.error("数据库连接关闭失败。", e);
              } finally {
                     this.co = null;
              }
       }

       /**
        * 获取指定表的数据数量
        * 
        * @param tableName
        * @return
        * @throws SQLException 
        */
       public Long tableContentNum(String tableName) throws SQLException {
              return tableContentNum(tableName, "*");
       }

       /**
        * 获取指定表的数据数量
        * 
        * @param tableName
        * @return
        * @throws SQLException 
        */
       public Long tableContentNum(String tableName, String key) throws SQLException {
              return tableContentNum(tableName, key, null);
       }

       /**
        * 获取指定表的数据数量
        * 
        * @param tableName
        * @return
        * @throws SQLException 
        */
       public Long tableContentNum(String tableName, List<String> params) throws SQLException {
              return tableContentNum(tableName, "*", params);
       }

       /**
        * 获取指定表的数据数量
        * 
        * @param tableName
        * @return
        * @throws SQLException 
        */
       public Long tableContentNum(String tableName, String key, List<String> params) throws SQLException {
              String defaultKey = null;
              if (Tool.isEmpty(key)) {
                     defaultKey = "*";
              } else {
                     defaultKey = key;
              }
              Long rowCount = 0l;
              PreparedStatement stmt = null;
              ResultSet res = null;
              try {
                     long startTime = System.currentTimeMillis();
                     StringBuffer sql = new StringBuffer(
                                   "select count(" + defaultKey + ") from " + tableName + " where 1 = 1 ");
                     if (!Tool.isEmpty(params)) {
                            for (String param : params) {
                                   sql.append(" " + param);
                            }
                     }
                     Connection con = conn();
                     if (con != null) {
                            stmt = con.prepareStatement(sql.toString());
                            res = stmt.executeQuery();
                            if (res.next()) {
                                   rowCount = res.getLong(1);
                            }
                            logger.info("【" + tableName + "】：" + rowCount + "条，统计用时："
                                          + (System.currentTimeMillis() - startTime) + "ms");
                            closeResStmt(stmt, res);
                     } else {
                            logger.error("获取“" + tableName + "”条数失败：Connection为空--tableContentNum");
                     }
                     closeConn();
              } catch (SQLException e) {
                     logger.error("获取“" + tableName + "”内容数量失败。", e);
                     throw e;
              }
              return rowCount;
       }
}
