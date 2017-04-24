package com.yijia.elasticsearch;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.indices.IndexMissingException;

import com.yijia.elasticsearch.util.ESUtil;

public class SynchUtil {

       private static Logger logger = Logger.getLogger(SynchUtil.class);

       /**
        * 添加数据； 如果数据库不存在自动创建，如果表不存在自动创建
        * 
        * @param index
        *               数据库的名
        * @param type
        *               table
        * @param id
        * @param columnNameList
        *               列名
        * @param typeNameList
        *               类型名称
        * @param colMap
        * 
        *               <pre>
        *  &lt;String, Object&gt; key->列名； value->值
        *  如果是char类型，自动添加ik分词和不分词字段分别以“IK”和“NO”结尾
        *               </pre>
        */
       public static void indexDocument(String index, String type, String id, List<String> columnNameList,
                     List<String> typeNameList, Map<String, Object> colMap) {
              boolean isExist = ESUtil.isExistsIndex(index);
              if (true != isExist) {
                     logger.info("数据库：【" + index + "】不存在");
                     ESUtil.createIndex(index);
              }

              boolean isExistType = ESUtil.isExistsType(index, type);
              if (true != isExistType) {
                     logger.info("数据库：【" + index + "】-表：[" + type + "]不存在");
                     ESUtil.createTypeJDBC(index, type, columnNameList, typeNameList);
              }
              XContentBuilder builder = null;
              Object value = null;
              try {
                     builder = XContentFactory.jsonBuilder().startObject();
                     // 循环加入该行每一列的值
                     for (int i = 0; i < columnNameList.size(); i++) {
                            // 字段名
                            String columnName = columnNameList.get(i);
                            String typeName = typeNameList.get(i);
                            value = colMap.get(columnName);

                            if (typeName.toLowerCase().contains("char")) {
                                   String tmpValue = value + "";
                                   // 拼出IK分词的字段名
                                   // String columnNameIK = "field_"+columnName
                                   // + "IK";
                                   String columnNameIK = columnName + "IK";
                                   builder.field(columnNameIK, tmpValue.trim());
                                   // 拼出不分词的字段名
                                   // String columnNameNO = "field_"+columnName
                                   // + "NO";
                                   String columnNameNO = columnName + "NO";
                                   builder.field(columnNameNO, tmpValue.trim());
                                   builder.field(columnName, tmpValue.trim());
                            } else {
                                   // builder.field("field_"+columnName, value);
                                   builder.field(columnName, value);
                            }
                     }

                     builder.endObject();
              } catch (IOException e) {
                     logger.error("拼接builder失败。", e);
              }
              // ESUtil.bulkProcessorSave(builder, index, type, id);
              try {
                     ESUtil.client().prepareIndex(index, type, id).setSource(builder).execute();
                     String msg = null;
                     if (id != null) {
                            msg = "-id：" + id + "数据更新成功";
                            logger.info("数据库：【" + index + "】-表：[" + type + "]" + msg);
                     } 
              } catch (Exception e) {
                     e.printStackTrace();
              } finally {
                     ESUtil.closeClient();
              }

              // IndexResponse response = ESUtil.client().prepareIndex(index,
              // type, id).setSource(builder).execute().actionGet();
              //
              // if(response != null){
              // if(id != null && response.getVersion() > 1){
              // msg = "-id：" + id + "数据更新成功";
              // }else{
              // msg = "数据添加成功";
              // }
              // logger.info("数据库：【" + index + "】-表：[" + type + "]" + msg);
              // }
       }

       /**
        * 
        * 添加数据； 如果数据库不存在自动创建，如果表不存在自动创建
        * 
        * @param index
        *               数据库的名
        * @param type
        *               table
        * @param columnNameList
        *               列名
        * @param typeNameList
        *               类型名称
        * @param colMap
        * 
        *               <pre>
        *  &lt;String, Object&gt; key->列名； value->值
        *  如果是char类型，自动添加ik分词和不分词字段分别以“IK”和“NO”结尾
        *               </pre>
        */
       public static void indexDocument(String index, String type, List<String> columnNameList,
                     List<String> typeNameList, Map<String, Object> colMap) {
              indexDocument(index, type, null, columnNameList, typeNameList, colMap);
       }

       /**
        * 
        * 对最后一条数据进行记录，保证数据的完整性
        * 
        * @param columnNameList
        *               列名
        * @param colMap
        * 
        *               <pre>
        *  &lt;String, Object&gt; key->列名，Value->值
        *  如果是char类型，自动添加ik分词和不分词字段分别以“IK”和“NO”结尾
        *               </pre>
        * 
        * @param index
        *               相当于数据库的名
        * @param type
        *               相当于table
        */
       public static void indexLast(String index, String type, List<String> columnNameList, List<String> typeNameList,
                     Map<String, Object> colMap) {
              indexDocument(index, type + "lastdata", "1", columnNameList, typeNameList, colMap);
       }

       /**
        * 获取最后一条数据
        * 
        * @param index
        * @param type
        * @return
        */
       public static Map<String, Object> getLastData(String index, String type) {
              Map<String, Object> source = null;
              try {
                     GetResponse response = ESUtil.client().prepareGet(index, type + "lastdata", "1").execute()
                                   .actionGet();
                     source = response.getSource();
              } catch (IndexMissingException e) {
                     logger.info("数据库：【" + index + "】不存在");
                     source = new HashMap<String, Object>();
              } catch (NoNodeAvailableException e) {
                     throw e;
              } catch (Exception e) {
                     logger.error("数据库：【" + index + "】-表：[" + type + "lastdata]-id:1 出错了", e);
                     source = new HashMap<String, Object>();
              }
              if (source == null) {
                     source = new HashMap<String, Object>();
              }
              return source;
       }

       /**
        * 获取最后一条数据的版本
        * 
        * @param index
        * @param type
        * @return
        */
       public static Long getLastDataVersion(String index, String type) {
              Long lastDataVersion = 0l;
              try {
                     GetResponse response = ESUtil.client().prepareGet(index, type + "lastdata", "1").execute()
                                   .actionGet();
                     lastDataVersion = response.getVersion();
              } catch (IndexMissingException e) {
                     logger.info("数据库：【" + index + "】不存在");
              } catch (NoNodeAvailableException e) {
                     throw e;
              } catch (Exception e) {
                     logger.error("数据库：【" + index + "】-表：[" + type + "lastdata]-id:1 出错了", e);
              } finally {
                     ESUtil.closeClient();
              }
              return lastDataVersion;
       }

       /**
        * 关闭client
        */
       public static void closeClient() {
              ESUtil.closeClient();
       }

       /**
        * 销毁es链接池
        */
       public static void destory() {
              ESUtil.destory();
       }

}