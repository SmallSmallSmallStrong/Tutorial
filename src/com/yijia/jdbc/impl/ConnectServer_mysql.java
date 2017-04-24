package com.yijia.jdbc.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.yijia.elasticsearch.SynchUtil;
import com.yijia.elasticsearch.TypeJDBCES;
import com.yijia.elasticsearch.bean.ESBeanConfig;
import com.yijia.jdbc.util.JDBCUtil_mysql;
import com.yijia.util.Tool;
import com.yijia.util.ToolAdd;
import com.yijia.util.ToolES;

public class ConnectServer_mysql extends ConnectServerImpl {

       private static Logger    logger         = Logger.getLogger(ConnectServer_mysql.class);

       protected JDBCUtil_mysql jdbcUtil_mysql = null;

       public ConnectServer_mysql(JDBCUtil_mysql jdbcUtil_mysql) {
              super(jdbcUtil_mysql);
              this.jdbcUtil_mysql = jdbcUtil_mysql;
       }

       @Override
       protected void init() {
              if (!ToolAdd.isEmpty(tableList)) {
                     return;
              }
              tableList.addAll(jdbcUtil_mysql.getTableList());
       }

       /**
        * 在es中创建索引插入数据（快速），找到最后一条的条数，从最后一条继续插入
        * 
        * @param tableName
        */
       public void creatESFast(String tableName) {
              String index = ESBeanConfig.getESBean().getStationtoen().toLowerCase();
              // 上次同步后，如果添加了新数据，这就是新数据第一条数据索引
              Long newDataIndex = SynchUtil.getLastDataVersion(index, tableName);
              PreparedStatement stmt = null;
              ResultSet res = null;
              try {
                     List<String> columnNameList = new ArrayList<String>();
                     // 字段类型list
                     List<String> typeNameList = new ArrayList<String>();

                     Map<String, List<String>> columnMap = jdbcUtil_mysql.getColumn(tableName);
                     if (!Tool.isEmpty(columnMap)) {
                            columnNameList.addAll(columnMap.get("columnNameList"));
                            typeNameList.addAll(columnMap.get("typeNameList"));
                     }
                     // 在数据库里查询出来的内容数量
                     Long count = jdbcUtil_mysql.tableContentNum(tableName);
                     if (count == null || count <= 0) {
                            return;
                     }
                     for (long n = newDataIndex; n < count; n += synchNum) {
                            String sql = "select * from " + tableName + " where id > " + n + " order by id asc limit "
                                          + synchNum;
                            logger.info(sql);
                            stmt = jdbcUtil_mysql.conn().prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                                          ResultSet.CONCUR_READ_ONLY);
                            res = stmt.executeQuery();
                            while (res.next()) {
                                   // 列和数据
                                   Map<String, Object> colMap = new HashMap<String, Object>();
                                   // 数据list
                                   ArrayList<Object> list = new ArrayList<Object>();
                                   putData(res, columnNameList, typeNameList, colMap, list);
                                   // 如果上一条数据是同步的最后一条数据或者最后的数据是null，同步数据
                                   insertDate(index, tableName, columnNameList, typeNameList, colMap);
                            }
                            logger.info("查询完成");
                            // 每次使用后，关闭PreparedStatement ResultSet（防止创建多个导致内存溢出）
                            JDBCUtil_mysql.closeResStmt(stmt, res);
                     }
              } catch (SQLException e) {
                     logger.error("获取查询信息失败。", e);
              } finally {
                     JDBCUtil_mysql.closeResStmt(stmt, res);
              }
       }

       /**
        * 在es中创建索引插入数据
        * 
        * @param tableName
        */
       public void creatES(String tableName) {


              String index = ESBeanConfig.getESBean().getStationtoen().toLowerCase();
              // 上次同步后的最后一条数据
              Map<String, Object> lastdata = SynchUtil.getLastData(index, tableName);
              // 所有的数据与上次同步后的最后一条数据是否相等，如果相等从下一条开始同步，否则从头开始同步数据
              boolean nextIsNewValue = false;
              PreparedStatement stmt = null;
              ResultSet res = null;
              try {
                     // 字段名称list
                     List<String> columnNameList = new ArrayList<String>();
                     // 字段类型list
                     List<String> typeNameList = new ArrayList<String>();

                     Map<String, List<String>> columnMap = jdbcUtil_mysql.getColumn(tableName);
                     if (!Tool.isEmpty(columnMap)) {
                            columnNameList.addAll(columnMap.get("columnNameList"));
                            typeNameList.addAll(columnMap.get("typeNameList"));
                     }
                     // 在数据库里查询出来的内容数量
                     Long count = jdbcUtil_mysql.tableContentNum(tableName);
                     if (count == null || count <= 0) {
                            return;
                     }
                     for (int n = 0; n < count; n += synchNum) {
                            String sql = "select * from " + tableName + " limit " + n + "," + synchNum;
                            stmt = jdbcUtil_mysql.conn().prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                                          ResultSet.CONCUR_READ_ONLY);
                            res = stmt.executeQuery();
                            while (res.next()) {
                                   // 列和数据
                                   Map<String, Object> colMap = new HashMap<String, Object>();
                                   // 数据list
                                   ArrayList<Object> list = new ArrayList<Object>();

                                   putData(res, columnNameList, typeNameList, colMap, list);
                                   // 如果上一条数据是同步的最后一条数据或者最后的数据是null，同步数据
                                   if (true == nextIsNewValue || lastdata == null || lastdata.isEmpty()) {
                                          insertDate(index, tableName, columnNameList, typeNameList, colMap);
                                   } else {
                                          // 与上次同步的相等的字段数量，每相等一个则数据加一，最后如果和本条数据字段数量相同则对以后的数据进行同步
                                          int eqNum = 0;
                                          for (int y = 0; y < list.size(); y++) {
                                                 String column = columnNameList.get(y);
                                                 String type = typeNameList.get(y);
                                                 // 读取的数据
                                                 Object value = list.get(y);
                                                 // 最后的数据
                                                 Object lastValue = lastdata.get(column);
                                                 if (type.toLowerCase().equals(TypeJDBCES.TIMESTAMP.getJdbc())) {
                                                        Date dateLast = ToolES.timeZone2Date(lastValue + "");
                                                        lastValue = dateLast;
                                                        value = Tool.parseTime(value + "", "yyyy-MM-dd HH:mm:ss.S");
                                                 }
                                                 // 如果当前条数和最后一条数据都不为空
                                                 if (value != null && lastValue != null) {
                                                        // 如果当前条数不是最后一条
                                                        if (!(value.toString().equals(lastValue.toString()))) {
                                                               break;
                                                        } else {
                                                               eqNum++;
                                                        }
                                                 } else {
                                                        if (value == null && lastValue == null) {
                                                               eqNum++;
                                                        }
                                                 }
                                          }
                                          if (eqNum == list.size()) {
                                                 nextIsNewValue = true;
                                          }
                                   }
                            }
                            // 每次使用后，关闭PreparedStatement ResultSet（防止创建多个导致内存溢出）
                            JDBCUtil_mysql.closeResStmt(stmt, res);
                     }
              } catch (SQLException e) {
                     logger.error("获取查询信息失败。", e);
              } finally {
                     JDBCUtil_mysql.closeResStmt(stmt, res);
              }
       }
}
