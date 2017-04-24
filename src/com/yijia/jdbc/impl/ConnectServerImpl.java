package com.yijia.jdbc.impl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.yijia.elasticsearch.SynchUtil;
import com.yijia.elasticsearch.TypeJDBCES;
import com.yijia.elasticsearch.bean.ESBeanConfig;
import com.yijia.jdbc.ConnectServer;
import com.yijia.jdbc.util.JDBCUtil;
import com.yijia.util.Tool;
import com.yijia.util.ToolAdd;

public abstract class ConnectServerImpl implements ConnectServer{

	private static Logger logger = Logger.getLogger(ConnectServerImpl.class);

    /** 每次同步数据，单次分页查询最大量 */
	protected Integer synchNum = Tool.sToI(ESBeanConfig.getESBean().getSynchNum(), 1000);
	/** 数据库表名 */
	protected List<Object> tableList = new ArrayList<Object>();
	/** 指定同步表名(*代表全部，多个表用前后逗号区分，例如：,z_user,z_role,) */
    private String synchTableName = ESBeanConfig.getESBean().getSynchTableName();
    
    private JDBCUtil jdbcUtil = null;
    
    public ConnectServerImpl(JDBCUtil jdbcUtil) {
        this.jdbcUtil = jdbcUtil;
    }
    //TODO
    protected abstract void init();

	/**
	 * 获取数据库表名 对每一个表同步数据
	 * 
	 * @param synchTableName 指定同步表名(null表示根据配置文件， *代表全部，多个表用前后逗号区分，例如：,z_user,z_role,)
	 */
	public void synch() {
		init();
		if (!ToolAdd.isEmpty(tableList)) {
		    String trueSynchTableName = Tool.isEmpty(synchTableName) ? "*" : synchTableName.trim();
			for (Object tableName : tableList) {
				String name = tableName.toString();
				String trueTableName = null;
				if(name == null){
				    continue;
				}else{
				    trueTableName = name.toLowerCase();
				}
				if("*".equals(trueSynchTableName) || trueSynchTableName.contains("," + trueTableName + ",")){
//				    creatES(trueTableName);
				    creatESFast(trueTableName);
				}
			}
		}
	}

	/**
	 * 将res中的数据插入colMap和list中
	 * 
	 * @param res
	 * @param columnNameList
	 * @param typeNameList
	 * @param colMap
	 * @param list
	 */
	protected void putData(ResultSet res, List<String> columnNameList, List<String> typeNameList,
			Map<String, Object> colMap, ArrayList<Object> list) {

		try {
			for (int i = 0; i < columnNameList.size(); i++) {
				// 列名
				String columnName = columnNameList.get(i);
				// 类型
				String typeName = typeNameList.get(i);
				// 数据临时变量
				Object tempValue = "";

				if (typeName.toLowerCase().equals(TypeJDBCES.BIT.getJdbc())) {
					tempValue = res.getBoolean(columnName);
					list.add(tempValue);
				}

				if (typeName.toLowerCase().equals(TypeJDBCES.NUMERIC.getJdbc())) {
					BigDecimal valueTmp = res.getBigDecimal(columnName);
					if (valueTmp != null) {
						tempValue = valueTmp;
						list.add(tempValue);
					}
				}

				if (typeName.toLowerCase().equals(TypeJDBCES.DOUBLE.getJdbc())) {
					tempValue = res.getDouble(columnName);
					list.add(tempValue);
				}

				if (typeName.toLowerCase().equals(TypeJDBCES.FLOAT.getJdbc())
						|| typeName.toLowerCase().equals(TypeJDBCES.REAL.getJdbc())) {
					tempValue = res.getFloat(columnName);
					list.add(tempValue);
				}

				if (typeName.toLowerCase().equals(TypeJDBCES.BIGINT.getJdbc())) {
					tempValue = res.getLong(columnName);
					list.add(tempValue);
				}

				if (typeName.toLowerCase().equals(TypeJDBCES.INT.getJdbc())
						|| typeName.toLowerCase().equals(TypeJDBCES.INTEGER.getJdbc())) {
					tempValue = res.getInt(columnName);
					list.add(tempValue);
				}

				if (typeName.toLowerCase().equals(TypeJDBCES.SMALLINT.getJdbc())) {
					tempValue = res.getShort(columnName);
					list.add(tempValue);
				}

				if (typeName.toLowerCase().equals(TypeJDBCES.TINYINT.getJdbc())) {
					tempValue = res.getByte(columnName);
					list.add(tempValue);
				}
				
				if (typeName.toLowerCase().equals(TypeJDBCES.TIMESTAMP.getJdbc())
				        || typeName.toLowerCase().equals(TypeJDBCES.DATETIME.getJdbc())) {
				    Date date = res.getTimestamp(columnName);
				    // 这是将ES中存储的date指定格式，不使用默认的格式化方式
				    // tempValue = Tool.formatTime(date, Tool.FORMAT, "");
				    tempValue = date == null ? Tool.parseTime("1999-01-01 00:00:00") : date;
				    list.add(tempValue);
				}

				if (typeName.toLowerCase().equals(TypeJDBCES.DATE.getJdbc())) {
					Date date = res.getDate(columnName);
					// 这是将ES中存储的date指定格式，不使用默认的格式化方式
					// tempValue = Tool.formatTime(date, Tool.FORMAT, "");
                    tempValue = date == null ? Tool.parseTime("1999-01-01", Tool.FORMAT_DAY) : date;
					list.add(tempValue);
				}

				if (typeName.toLowerCase().equals(TypeJDBCES.TIME.getJdbc())) {
					Date date = res.getTime(columnName);
					// 这是将ES中存储的date指定格式，不使用默认的格式化方式
					// tempValue = Tool.formatTime(date, Tool.FORMAT, "");
                    tempValue = date == null ? Tool.parseTime("00:00:00", "HH:mm:ss") : date;
					list.add(tempValue);
				}

				if (typeName.toLowerCase().equals(TypeJDBCES.VARCHAR.getJdbc())
						|| typeName.toLowerCase().equals(TypeJDBCES.CHAR.getJdbc())) {
					tempValue = res.getString(columnName);
					String tmp = tempValue + "";
					list.add(tmp.trim());
				}

				if (typeName.toLowerCase().equals(TypeJDBCES.NVARCHAR.getJdbc())
						|| typeName.toLowerCase().equals(TypeJDBCES.NCHAR.getJdbc())
						|| typeName.toLowerCase().equals(TypeJDBCES.LONGVARCHAR.getJdbc())) {
					tempValue = res.getNString(columnName);
                    String tmp = tempValue + "";
                    list.add(tmp.trim());
				}

				colMap.put(columnName, tempValue);
			}
		} catch (SQLException e) {
			logger.error("读取数据失败", e);
		}
	}

	/**
	 * 插入数据，并更新记录表数据
	 * 
	 * @param index
	 * @param tableName
	 * @param columnNameList
	 * @param typeNameList
	 * @param colMap
	 */
	protected void insertDate(String index, String tableName, List<String> columnNameList, List<String> typeNameList,
			Map<String, Object> colMap) {
		SynchUtil.indexDocument(index, tableName.toLowerCase(), columnNameList, typeNameList, colMap);
		// 更新最后的数据
		SynchUtil.indexLast(index, tableName.toLowerCase(), columnNameList, typeNameList, colMap);
	}

    /**
     * 关闭数据库和ES连接销毁连接池
     */
    public void close() {
        jdbcUtil.closeConn();
        SynchUtil.closeClient();
        SynchUtil.destory();
    }
    
}
