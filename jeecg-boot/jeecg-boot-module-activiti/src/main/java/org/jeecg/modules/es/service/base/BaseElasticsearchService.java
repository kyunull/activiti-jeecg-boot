package org.jeecg.modules.es.service.base;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.HttpAsyncResponseConsumerFactory;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.jeecg.modules.es.config.ElasticsearchProperties;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 基础es服务
 *
 * @author dongjb
 * @version 1.0v
 * @since 2021-09-03
 */
@Slf4j
public abstract class BaseElasticsearchService {

    @Resource
    protected RestHighLevelClient client;

    @Resource
    private ElasticsearchProperties elasticsearchProperties;

    protected static final RequestOptions COMMON_OPTIONS;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();

        // 默认缓冲限制为100MB，此处修改为30MB。
        builder.setHttpAsyncResponseConsumerFactory(new HttpAsyncResponseConsumerFactory.HeapBufferedResponseConsumerFactory(30 * 1024 * 1024));
        COMMON_OPTIONS = builder.build();
    }

    /**
     * 创建索引库
     *
     * @param index 索引库名称
     */
    protected void createIndexRequest(String index) {
        try {
            CreateIndexRequest request = new CreateIndexRequest(index);
            // Settings for this index
            request.settings(Settings.builder().put("index.number_of_shards", elasticsearchProperties.getIndex().getNumberOfShards()).put("index.number_of_replicas", elasticsearchProperties.getIndex().getNumberOfReplicas()));

            CreateIndexResponse createIndexResponse = client.indices().create(request, COMMON_OPTIONS);

            log.info(" whether all of the nodes have acknowledged the request : {}", createIndexResponse.isAcknowledged());
            log.info(" Indicates whether the requisite number of shard copies were started for each shard in the index before timing out :{}", createIndexResponse.isShardsAcknowledged());
        } catch (IOException e) {
            throw new ElasticsearchException("创建索引 {" + index + "} 失败");
        }
    }

    /**
     * 删除索引库
     *
     * @param index 索引库名称
     */
    protected void deleteIndexRequest(String index) {
        DeleteIndexRequest deleteIndexRequest = buildDeleteIndexRequest(index);
        try {
            client.indices().delete(deleteIndexRequest, COMMON_OPTIONS);
        } catch (IOException e) {
            throw new ElasticsearchException("删除索引 {" + index + "} 失败");
        }
    }

    /**
     * 构建删除索引请求
     *
     * @param index 索引名称
     */
    private static DeleteIndexRequest buildDeleteIndexRequest(String index) {
        return new DeleteIndexRequest(index);
    }

    /**
     * 插入索引记录
     *
     * @param index  索引库名称
     * @param id     索引标识
     * @param object 索引数据
     * @return {@link org.elasticsearch.action.index.IndexRequest}
     */
    protected static IndexRequest buildIndexRequest(String index, String id, Object object) {
        return new IndexRequest(index).id(id).source(BeanUtil.beanToMap(object), XContentType.JSON);
    }

    /**
     * 更新索引记录
     *
     * @param index  索引库名称
     * @param id     索引标识
     * @param object 索引数据
     */
    protected void updateRequest(String index, String id, Object object) {
        try {
            UpdateRequest updateRequest = new UpdateRequest(index, id).doc(BeanUtil.beanToMap(object), XContentType.JSON);
            client.update(updateRequest, COMMON_OPTIONS);
        } catch (IOException e) {
            throw new ElasticsearchException("更新索引 {" + index + "} 数据 {" + object + "} 失败");
        }
    }

    /**
     * 删除索引记录
     *
     * @param index 索引库名称
     * @param id    索引标识
     */
    protected void deleteRequest(String index, String id) {
        try {
            DeleteRequest deleteRequest = new DeleteRequest(index, id);
            client.delete(deleteRequest, COMMON_OPTIONS);
        } catch (IOException e) {
            throw new ElasticsearchException("删除索引 {" + index + "} 数据id {" + id + "} 失败");
        }
    }

    /**
     * 查询全部索引记录
     *
     * @param index 索引库名称
     * @return {@link SearchResponse}
     */
    protected SearchResponse searchAll(String index) {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;
        try {
            searchResponse = client.search(searchRequest, COMMON_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchResponse;
    }

    protected List<Map<String, Object>> searchDataPage(String index, int startPage, int pageSize,
                                                       SearchSourceBuilder sourceBuilder, QueryBuilder queryBuilder) {
        SearchRequest request = new SearchRequest(index);
        //设置超时时间
        sourceBuilder.timeout(new TimeValue(120, TimeUnit.SECONDS));
        //设置是否按照匹配度排序
        sourceBuilder.explain(true);
        //加载查询条件
        sourceBuilder.query(queryBuilder);
        //设置分页
        sourceBuilder.from((startPage- 1) * pageSize).size(pageSize);
        log.info("查询返回条件：{}", sourceBuilder);
        request.source(sourceBuilder);
        try {
            SearchResponse searchResponse = client.search(request, COMMON_OPTIONS);
            long totalHits = searchResponse.getHits().getTotalHits().value;
            log.info("共查出{}条记录", totalHits);
            RestStatus status = searchResponse.status();
            if(status.getStatus() == 200) {
                List<Map<String, Object>> sourcList = new ArrayList<>();
                for (SearchHit searchHit : searchResponse.getHits().getHits()) {
                    Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                    sourcList.add(sourceAsMap);
                }
                return sourcList;
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.info("条件查询索引{}时出错", index);
        }
        return null;
    }
}
