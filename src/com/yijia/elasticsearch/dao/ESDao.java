package com.yijia.elasticsearch.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.FacetBuilder;

import com.yijia.bean.Page;
import com.yijia.elasticsearch.bean.ESBean;
import com.yijia.elasticsearch.bean.ESBeanConfig;
import com.yijia.util.Tool;

public class ESDao {
    
    private static Logger logger = Logger.getLogger(ESDao.class);

    /** 获取连接 */
    private static Client client;
    private static ESBean esBean = ESBeanConfig.getESBean();
    private static BulkProcessor bulkProcessor;

    
    /**
     * 获取client
     * 
     * @return
     */
    @SuppressWarnings("resource")
    public Client client() {
        if (client == null) {
            synchronized (ESDao.class) {
                if(client == null){
                    Settings settings = ImmutableSettings.settingsBuilder().put("client.transport.sniff", true)
                            .put("client.transport.ping_timeout", "300s")
                            .put("client.transport.ping_retries", "1000")
                            .put("client.transport.ping_interval", "60s")
                            .put("cluster.name", esBean.getClusterName()).build();
                    client = new TransportClient(settings).addTransportAddress(
                            new InetSocketTransportAddress(esBean.getHost(), Tool.sToI(esBean.getPort(), 9300)));
                }
            }
        }
        return client;
    }

    /**
     * 
     * @param builder
     * @param indexName
     * @param typeName
     */
    public void saveData(XContentBuilder builder, String indexName, String typeName, String id) {

        if (bulkProcessor == null) {
            synchronized(ESDao.class){
                if(bulkProcessor == null){
                    bulkProcessor = BulkProcessor.builder(client(), new BulkProcessor.Listener() {
                        public void beforeBulk(long executionId, BulkRequest request) {
                            logger.debug("开始前");
                        }
            
                        public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                            logger.debug("完成");
                        }
            
                        public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                            logger.debug(failure.getMessage(), failure);
                        }
                    }).setBulkActions(10000).setBulkSize(new ByteSizeValue(1, ByteSizeUnit.GB))
                            .setFlushInterval(TimeValue.timeValueSeconds(5)).setConcurrentRequests(9).build();
                }
            }
        }
        
        bulkProcessor.add(new IndexRequest(indexName, typeName, id).source(builder) );
    }

    /***
     * 修改
     * 
     * @param builder
     * @param indexName
     * @param typeName
     * @param id
     * @throws ElasticsearchException
     */
    public void updateData(XContentBuilder builder, String indexName, String typeName, String id)
            throws ElasticsearchException {
        client().prepareUpdate(indexName, typeName, id).setDoc(builder).get();
        this.closeClient();
    }

    /**
     * 删除
     * 
     * @param indexname
     *            为索引库名，一个es集群中可以有多个索引库。 名称必须为小写
     * @param type
     *            Type为索引类型，是用来区分同索引库下不同类型的数据的，一个索引库下可以有多个索引类型。
     * @param id
     *            索引对象单独指定ID
     */
    public boolean delteIndexResponse(String indexname, String type, String id) {
        try {
            if ((id != null) && (!id.isEmpty())) {
                DeleteResponse response = client().prepareDelete(indexname, type, id).execute().actionGet();
                if (response.isFound()) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error("多线程保存关闭是出错。");
        }
        this.closeClient();
        return false;
    }

    /***
     * 新增type字段
     * 
     * @param xContentBuilder
     * @param indexname
     * @param type
     * @throws ElasticsearchException
     */
    public void upTypeField(XContentBuilder xContentBuilder, String indexname, String type) throws ElasticsearchException {
        PutMappingRequest request = new PutMappingRequest(indexname);
        request.type(type);
        request.source(xContentBuilder);
        client().admin().indices().putMapping(request).actionGet();
        this.closeClient();
    }
    
    /**
     * 基础查询
     * 
     * @param page
     * @param indexname
     * @param type
     * @param queryBuilderList
     * @param filterBuilderList
     * @param aggregation
     * @return
     */
    private SearchHits search(Page page, String indexname, String type, 
            List<QueryBuilder> queryBuilderList, 
            List<FilterBuilder> filterBuilderList) {
        SearchResponse response = null;
        SearchRequestBuilder queryBuild = client().prepareSearch(indexname).setTypes(type);
        if(queryBuilderList != null && queryBuilderList.size() > 0){
            for(QueryBuilder queryBuilder : queryBuilderList){
                queryBuild.setQuery(queryBuilder);
            }
        }
        if(filterBuilderList != null && filterBuilderList.size() > 0){
            for(FilterBuilder postFilter : filterBuilderList){
                queryBuild.setPostFilter(postFilter);
            }
        }
        if(page != null){
            queryBuild.setFrom(page.getFirstRow()).setSize(page.getPageSize());
        }
        response = queryBuild.execute().actionGet();
        return response.getHits();
    }
    
    /**
     * 根据queryBuilder查询
     * 
     * @param page
     * @param indexname
     * @param type
     * @param queryBuilderList
     * @return
     */
    public SearchHits search(Page page, String indexname, String type, List<QueryBuilder> queryBuilderList) {
        return search(page, indexname, type, queryBuilderList, null);
    }
    
    /**
     * 根据queryBuilder查询
     * 
     * @param indexname
     * @param type
     * @param queryBuilderList
     * @return
     */
    public SearchHits search(String indexname, String type, List<QueryBuilder> queryBuilderList) {
        return search(initPage(), indexname, type, queryBuilderList);
    }
    
    /**
     * 根据queryBuilder查询
     * 
     * @param page
     * @param indexname
     * @param type
     * @param queryBuilder
     * @return
     */
    public SearchHits search(Page page, String indexname, String type, QueryBuilder queryBuilder) {
        return search(page, indexname, type, initQueryBuilder(queryBuilder));
    }

    /**
     * 根据queryBuilder查询
     * 
     * @param indexname
     * @param type
     * @param queryBuilder
     * @return
     */
    public SearchHits search(String indexname, String type, QueryBuilder queryBuilder) {
        return search(initPage(), indexname, type, queryBuilder);
    }
    
    /**
     * 根据queryBuilder查询
     * 
     * @param page
     * @param indexname
     * @param type
     * @param queryBuilder
     * @param filterBuilderList
     * @return
     */
    public SearchHits search(Page page, String indexname, String type, QueryBuilder queryBuilder, List<FilterBuilder> filterBuilderList) {
        return search(initPage(), indexname, type, initQueryBuilder(queryBuilder), filterBuilderList);
    }
    
    /**
     * 根据queryBuilder查询
     * 
     * @param indexname
     * @param type
     * @param queryBuilder
     * @param filterBuilderList
     * @return
     */
    public SearchHits search(String indexname, String type, QueryBuilder queryBuilder, List<FilterBuilder> filterBuilderList) {
        return search(initPage(), indexname, type, queryBuilder, filterBuilderList);
    }
    
    /**
     * 使用Aggregation统计
     * 
     * @param page
     * @param indexname
     * @param type
     * @param aggregation
     * @return
     */
    public Map<String, Aggregation> search(Page page, String indexname, String type, AbstractAggregationBuilder aggregation) {
        SearchRequestBuilder queryBuild = client().prepareSearch(indexname).setTypes(type).addAggregation(aggregation);

        if(page != null){
            queryBuild.setFrom(page.getFirstRow()).setSize(page.getPageSize());
        }
        SearchResponse response = queryBuild.execute().actionGet();
        if(response != null){
            return response.getAggregations().asMap();
        }else{
            return new HashMap<String, Aggregation>();
        }
    }
    
    /**
     * 使用Aggregation统计
     * 
     * @param page
     * @param indexname
     * @param type
     * @param aggregation
     * @return
     */
    public Map<String, Aggregation> searchAll(String indexname, String type, AbstractAggregationBuilder aggregation) {
        SearchRequestBuilder queryBuild = client().prepareSearch(indexname).setTypes(type).addAggregation(aggregation);
        queryBuild.setQuery(QueryBuilders.matchAllQuery());
        SearchResponse response = queryBuild.execute().actionGet();
        if(response != null){
            return response.getAggregations().asMap();
        }else{
            return new HashMap<String, Aggregation>();
        }
    }
    
    /**
     * 使用Aggregation统计
     * 
     * @param indexname
     * @param type
     * @param aggregation
     * @return
     */
    public Map<String, Aggregation> search(String indexname, String type, AbstractAggregationBuilder aggregation) {
        return search(initPage(), indexname, type, aggregation);
    }

    /**
     * 使用Facet统计
     * 
     * @param page
     * @param indexname
     * @param type
     * @param facetBuilder
     * @return
     */
    @SuppressWarnings("deprecation")
    public Map<String, Facet> search(Page page, String indexname, String type, FacetBuilder facetBuilder) {
        SearchRequestBuilder queryBuild = client().prepareSearch(indexname).setTypes(type).addFacet(facetBuilder);
        if(page != null){
            queryBuild.setFrom(page.getFirstRow()).setSize(page.getPageSize());
        }
        SearchResponse response = queryBuild.execute().actionGet();
        if(response != null){
            return response.getFacets().facetsAsMap();
        }else{
            return new HashMap<String, Facet>();
        }
    }
    
    /**
     * 使用Facet统计
     * 
     * @param indexname
     * @param type
     * @param facetBuilder
     * @return
     */
    public Map<String, Facet> search(String indexname, String type, FacetBuilder facetBuilder) {
        return search(initPage(), indexname, type, facetBuilder);
    }
    
    /**
     * 初始化Page，进行分页
     * 
     * @return
     */
    public Page initPage(){
        Page page = new Page();
        page.setRowTotal(0l);
        page.setCurentPage(1);
        page.setPageSize(Integer.MAX_VALUE);
        return page;
    }
    
    /**
     * 将queryBuilder转为List&lt;QueryBuilder&gt;
     * 
     * @param queryBuilder
     * @return
     */
    public List<QueryBuilder> initQueryBuilder(QueryBuilder queryBuilder){
        if(queryBuilder == null){
            return null;
        }
        List<QueryBuilder> queryBuilderList = new ArrayList<QueryBuilder>();
        queryBuilderList.add(queryBuilder);
        return queryBuilderList;
    }
    
    /**
     * 将filterBuilder转为List&lt;FilterBuilder&gt;
     * 
     * @param filterBuilder
     * @return
     */
    public List<FilterBuilder> initFilterBuilder(FilterBuilder filterBuilder){
        if(filterBuilder == null){
            return null;
        }
        List<FilterBuilder> filterBuilderList = new ArrayList<FilterBuilder>();
        filterBuilderList.add(filterBuilder);
        return filterBuilderList;
    }

    /**
     * 关闭BulkProcessor
     * 阻塞至所有的请求线程处理完毕后，断开连接资源
     */
    public void closeBulkProcessor() {

        if (bulkProcessor != null) {
            try {
                bulkProcessor.awaitClose(3, TimeUnit.MINUTES);
            } catch (Exception e) {
                logger.error("多线程保存关闭是出错。");
            } finally {
                bulkProcessor = null;
            }
        }
    }

    public void closeClient() {
        try {
            if(client != null){
                client.close();
            }
        } catch (Exception e) {
            logger.error("es client 关闭失败", e);
        } finally {
            client = null;
        }
    }
    
    /**
     * 关闭批量插入和连接
     */
    public void close(){
        closeBulkProcessor();
        closeClient();
    }

}
