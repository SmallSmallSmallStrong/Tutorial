package com.yijia.elasticsearch.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.indices.IndexMissingException;

import com.yijia.elasticsearch.TypeJDBCES;
import com.yijia.elasticsearch.bean.ESBean;
import com.yijia.elasticsearch.bean.ESBeanConfig;
import com.yijia.elasticsearch.dao.ESDao;
import com.yijia.pool.ESConnectionPool;
import com.yijia.util.Tool;

/**
 * elasticsearch工具类
 * 
 * @author zhqy
 *
 */
public class ESUtil {

	private static Logger logger = Logger.getLogger(ESUtil.class);
	private static Client client;
	private static ESConnectionPool esConnectionPool;
	private static ESBean esBean = ESBeanConfig.getESBean();

	private ESUtil() {

	}

	/**
	 * 初始化client
	 * 
	 * @return
	 */
	public static Client client() {
		if (client == null) {
			if (esConnectionPool == null) {
				esConnectionPool = ESConnectionPool.getConnectionPool(esBean);
			}
			try {
				client = esConnectionPool.getClient();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		return client;
	}

	/**
	 * 关闭client，并将client和ESDao初始化为null
	 * 
	 */
	public static void closeClient() {
		if (client != null) {
			if (esConnectionPool != null) {
				esConnectionPool.realse(client);
			}
			client = null;
		}
	}
	/**
	 * 销毁线程池
	 */
	public static void destory() {
		if (client != null) {
			if (esConnectionPool != null) {
				esConnectionPool.destory();
			}
			client = null;
		}
	}

	/**
	 * 判断指定的索引名是否存在
	 * 
	 * @param index
	 *            索引名
	 * @return 存在：true; 不存在：false;
	 */
	public static boolean isExistsIndex(String index) {
		IndicesExistsResponse response = client().admin().indices()
				.exists(new IndicesExistsRequest().indices(new String[] { index })).actionGet();
		return response.isExists();
	}

	/**
	 * 判断指定的索引的类型是否存在
	 * 
	 * @param index
	 *            索引名
	 * @param type
	 *            索引类型
	 * @return 存在：true; 不存在：false;
	 */
	public static boolean isExistsType(String index, String type) {
		TypesExistsResponse response = client().admin().indices()
				.typesExists(new TypesExistsRequest(new String[] { index }, type)).actionGet();
		return response.isExists();
	}

	/**
	 * 创建数据库
	 * 
	 * @param index
	 */
	public static void createIndex(String index) {
		Settings settings = ImmutableSettings.settingsBuilder()
				// 5个主分片
				.put("number_of_shards", Tool.sToI(ESBeanConfig.getESBean().getNumberOfShards(), 5))
				// 测试环境，减少副本提高速度
				.put("number_of_replicas", Tool.sToI(ESBeanConfig.getESBean().getNumberOfReplicas(), 1)).build();
		// 首先创建索引库
		CreateIndexResponse indexresponse = client().admin().indices()
				// 这个索引库的名称还必须不包含大写字母
				.prepareCreate(index).setSettings(settings).execute().actionGet();
		if (true == indexresponse.isAcknowledged()) {
			logger.info("创建数据库：【" + index + "】成功");
		} else {
			logger.error("创建数据库：【" + index + "】失败");
		}
	}

	/**
	 * 
	 * 根据XContentBuilder进行表创建
	 * 
	 * @param index
	 *            索引名称；
	 * @param type
	 *            索引类型
	 * @param builder
	 *            建表类型
	 */
	public static void createMapping(String index, String type, XContentBuilder builder) {
		boolean isExist = isExistsIndex(index);
		if (true != isExist) {
			logger.info("数据库：【" + index + "】不存在");
			createIndex(index);
		}

		PutMappingRequest mapping = Requests.putMappingRequest(index).type(type).source(builder);
		try {
			client().admin().indices().putMapping(mapping).actionGet();
			logger.info("创建数据库：【" + index + "】-表：[" + type + "]成功");
		} catch (IndexMissingException e) {
			logger.error("创建数据库：【" + index + "】-表：[" + type + "]失败", e);
		}
	}

	/**
	 * 通过list创建表
	 * 
	 * @param index
	 *            索引名称；
	 * @param type
	 *            索引类型
	 * @param columnNameList
	 *            要创建的列名列表
	 * @param typeNameList
	 * 
	 *            <pre>
	 * 中的map&lt;String, String&gt; key->列名； value->值
	 * 最多3组数据：
	 *  "typeName"->"typeName"; 
	 *  "analyzer"->"ik如果是数字类型则为空"; 
	 *  "ext"->"Analyzer或者null";
	 * 要创建的列名对应类型列表
	 *            </pre>
	 */
	public static void createType(String index, String type, List<String> columnNameList,
			List<Map<String, String>> typeNameList) {

		boolean isExist = isExistsIndex(index);
		if (true != isExist) {
			logger.info("数据库：【" + index + "】不存在");
			createIndex(index);
		}

		try {
			XContentBuilder builder = XContentFactory.jsonBuilder().startObject().startObject(type)
					.startObject("properties");

			for (int i = 0; i < columnNameList.size(); i++) {
				String columnName = columnNameList.get(i);
				Map<String, String> typeNameMap = typeNameList.get(i);
				String typeName = typeNameMap.get("typeName");
				String analyzer = typeNameMap.get("analyzer");
				builder.startObject(columnName).field("type", typeName).field("store", "yes");
				// 这是将ES中存储的date指定格式，不使用默认的格式化方式
				// if(typeName.equals(TypeJDBCES.DATE.getJdbc())
				// || typeName.equals(TypeJDBCES.TIME.getJdbc())
				// || typeName.equals(TypeJDBCES.TIMESTAMP.getJdbc())){
				// builder.field("format", Tool.FORMAT);
				// }
				if (analyzer != null) {
					String ext = typeNameMap.get("ext");
					String indexAnalyzer = "index" + ext;
					String searchAnalyzer = "search" + ext;
					builder.field(indexAnalyzer, analyzer).field(searchAnalyzer, analyzer);
				}
				builder.endObject();
			}
			createMapping(index, type, builder);
		} catch (IOException e) {
			logger.error("构建数据库：【" + index + "】-表：[" + type + "]" + "的XContentBuilder失败", e);
		}
	}

	/**
	 * 
	 * @param index
	 *            索引名称；
	 * @param type
	 *            索引类型
	 * @param columnNameList
	 *            要创建的列名列表
	 * @param typeNameList
	 *            要创建的列名对应类型列表
	 */
	public static void createTypeJDBC(String index, String type, List<String> columnNameList,
			List<String> typeNameList) {

		boolean isExist = isExistsIndex(index);
		if (true != isExist) {
			logger.info("数据库：【" + index + "】不存在");
			createIndex(index);
		}

		try {
			XContentBuilder builder = XContentFactory.jsonBuilder().startObject().startObject(type)
					.startObject("properties");

			for (int i = 0; i < columnNameList.size(); i++) {
				String columnName = columnNameList.get(i);
				String typeName = typeNameList.get(i);

				for (TypeJDBCES typeJDBCES : TypeJDBCES.values()) {
					String lowerTypeName = typeName.toLowerCase();
					if (!lowerTypeName.equals(typeJDBCES.getJdbc())) {
						continue;
					}
					if (lowerTypeName.equals(TypeJDBCES.VARCHAR.getJdbc())
							|| lowerTypeName.equals(TypeJDBCES.CHAR.getJdbc())
							|| lowerTypeName.equals(TypeJDBCES.LONGVARCHAR.getJdbc())) {
						builder.startObject(columnName).field("type", TypeJDBCES.CHAR.getEs()).field("store", "yes")
								.field("index", "analyzed").field("search", "analyzed").endObject();
						builder.startObject(columnName + "IK").field("type", TypeJDBCES.CHAR.getEs())
								.field("store", "yes").field("indexAnalyzer", "ik").field("searchAnalyzer", "ik")
								.endObject();
						builder.startObject(columnName + "NO").field("type", TypeJDBCES.CHAR.getEs())
								.field("store", "yes").field("index", "not_analyzed").field("search", "not_analyzed")
								.endObject();
					} else {
						builder.startObject(columnName).field("type", typeJDBCES.getEs()).field("store", "yes");
						// 这是将ES中存储的date指定格式，不使用默认的格式化方式
						// if(lowerTypeName.equals(TypeJDBCES.DATE.getJdbc())
						// || lowerTypeName.equals(TypeJDBCES.TIME.getJdbc())
						// ||
						// lowerTypeName.equals(TypeJDBCES.TIMESTAMP.getJdbc())){
						// builder.field("format", Tool.FORMAT);
						// }
						if (!Tool.isEmpty(typeJDBCES.getAnalyzer())) {
							if ("ik".equals(typeJDBCES.getAnalyzer())) {
								builder.field("indexAnalyzer", "ik").field("searchAnalyzer", "ik");
							} else {
								builder.field("index", typeJDBCES.getAnalyzer()).field("search",
										typeJDBCES.getAnalyzer());
							}
						}
						builder.endObject();
					}
				}
			}
			createMapping(index, type, builder);
		} catch (IOException e) {
			logger.error("构建数据库：【" + index + "】-表：[" + type + "]" + "的XContentBuilder失败", e);
		}
	}

}
