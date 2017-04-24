package com.yijia.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.yijia.jdbc.util.JDBCUtil_mysql;
import com.yijia.util.Tool;

public class InsertThread implements Runnable {

    private static Logger logger = Logger.getLogger(InsertThread.class);
    
    protected JDBCUtil_mysql jdbcUtil_mysql = null;
    
    private String name;

    public InsertThread(String name) {
        this.name = name;
        if(jdbcUtil_mysql == null){
            jdbcUtil_mysql = new JDBCUtil_mysql();
        }
    }

    public void insert_old() {
        PreparedStatement stmt = null;
        String sql = "insert into test_b(`code`, `sort`, `content`, `pre`) values (?,?,?,?)";
//        String sql = "insert into test_a(`code`, `sort`, `content`, `pre`) values (?,?,?,?)";
        while (true) {
            long startTime = System.currentTimeMillis();
            Connection conn = jdbcUtil_mysql.conn();
            try {
                conn.setAutoCommit(false);
                stmt = conn.prepareStatement(sql);
                for(int i = 0; i < 1000; i++){
                    stmt.setString(1, Tool.randomStr(5));
                    stmt.setInt(2, Tool.randomNum(1, 10000));
                    stmt.setString(3, Tool.randomStr(2) + "-" + Tool.randomStr(3));
                    stmt.setString(4, System.currentTimeMillis() + "");
                    stmt.addBatch();
                }
                stmt.executeBatch();
                conn.commit();
                stmt.clearBatch();
                logger.info("线程【" + this.name + "】批量插入成功-1000条-用时：" + (System.currentTimeMillis() - startTime) + "毫秒。");
            } catch (SQLException e) {
                logger.error("线程【" + this.name + "】数据库语句【" + sql + "】执行出错。", e);
            } finally {
                JDBCUtil_mysql.closeStatement(stmt);
                jdbcUtil_mysql.closeConn();
            }
        }
    }
    
    public void insert() {
        PreparedStatement stmt = null;
        String sql = "insert into test_b(`code`, `sort`, `content`, `pre`) values (?,?,?,?)";
//        String sql = "insert into test_a(`code`, `sort`, `content`, `pre`) values (?,?,?,?)";
        int i = 1;
        long startTime = System.currentTimeMillis();
        while (true) {
            Connection conn = jdbcUtil_mysql.conn();
            try {
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, Tool.randomStr(5));
                stmt.setInt(2, Tool.randomNum(1, 10000));
                stmt.setString(3, Tool.randomStr(2) + "-" + Tool.randomStr(3));
                stmt.setString(4, System.currentTimeMillis() + "");
                stmt.executeUpdate();
                if (i % 1000 == 0) {
                    logger.info("线程【" + this.name + "】批量插入成功-1000条-用时：" + (System.currentTimeMillis() - startTime) + "毫秒。");
                    startTime = System.currentTimeMillis();
                }
                i++;
            } catch (SQLException e) {
                logger.error("线程【" + this.name + "】数据库语句【" + sql + "】执行出错。", e);
            } finally {
                JDBCUtil_mysql.closeStatement(stmt);
                jdbcUtil_mysql.closeConn();
            }
        }
    }

    @Override
    public void run() {
        insert();
//        insert_old();
    }
}
