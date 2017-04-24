package com.yijia.jdbc.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.yijia.elasticsearch.SynchUtil;
import com.yijia.elasticsearch.bean.ESBeanConfig;
import com.yijia.jdbc.util.JDBCUtil_DB2;
import com.yijia.util.Tool;
import com.yijia.util.ToolAdd;

public class ConnectServer_DB2 extends ConnectServerImpl {

       private static Logger  logger       = Logger.getLogger(ConnectServer_DB2.class);

       boolean                b            = false;

       protected JDBCUtil_DB2 jdbcUtil_DB2 = null;

       public ConnectServer_DB2(JDBCUtil_DB2 jdbcUtil_DB2) {
              super(jdbcUtil_DB2);
              this.jdbcUtil_DB2 = jdbcUtil_DB2;
       }

       @Override
       protected void init() {
              if (!ToolAdd.isEmpty(tableList)) {
                     return;
              }
              try {
                     tableList.addAll(jdbcUtil_DB2.getTableList());
              } catch (SQLException e) {
                     e.printStackTrace();
              }
       }

       /**
        * 在es中创建索引插入数据（快速），找到最后一条的条数，从最后一条继续插入
        * 
        * @param tableName
        */
       public void creatESFast(String tableName) {
              String orderFiledName = ESBeanConfig.getESBean().getSynchTableOrderFieldName();
              String orderFiled = null;
              try {
                     JSONObject jsonObject = new JSONObject(orderFiledName);
                     orderFiled = jsonObject.getString(tableName);
              } catch (JSONException e1) {
              }
              List<String> params = null;
              String orderSQL = null;

              if (!Tool.isEmpty(orderFiled)) {
                     params = new ArrayList<String>();
                     params.add(" and " + orderFiled + " > timestamp('2016-01-01 00:00:00') ");
                     orderSQL = " order by " + orderFiled + " asc ";
              }

              // 在数据库里查询出来的内容数量
              try {
                     Long count = jdbcUtil_DB2.tableContentNum(tableName, params);
                     if (count == null || count <= 0) {
                            return;
                     }
              } catch (SQLException e1) {
                     e1.printStackTrace();
              }
              String index = ESBeanConfig.getESBean().getStationtoen().toLowerCase();
              // 上次同步后，如果添加了新数据，这就是新数据第一条数据索引
              Long newDataIndex = SynchUtil.getLastDataVersion(index, tableName);
              PreparedStatement stmt = null;
              ResultSet res = null;
              try {
                     // 字段名称list
                     List<String> columnNameList = new ArrayList<String>();
                     // 字段类型list
                     List<String> typeNameList = new ArrayList<String>();

                     Map<String, List<String>> columnMap = jdbcUtil_DB2.getColumn(tableName);
                     if (!Tool.isEmpty(columnMap)) {
                            columnNameList.addAll(columnMap.get("columnNameList"));
                            typeNameList.addAll(columnMap.get("typeNameList"));
                     }
                     newDataIndex++;
                     String sql = jdbcUtil_DB2.pageSQL(tableName, params, orderSQL, newDataIndex, synchNum);
                     Connection con = jdbcUtil_DB2.conn();
                     if (con != null) {
                            stmt = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
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
                            // 每次使用后，关闭PreparedStatement ResultSet（防止创建多个导致内存溢出）
                            JDBCUtil_DB2.closeResStmt(stmt, res);
                            logger.info(tableName + " 同步 " + synchNum  + " 条数据"); 
                     }else{
                         logger.info("Connection为空无法查询db2数据库");
                     }
                     jdbcUtil_DB2.closeConn();
              } catch (SQLException e) {
                     logger.error("获取查询信息失败。", e);
              }
       }
}
