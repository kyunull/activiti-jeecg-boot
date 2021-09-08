package org.jeecg.modules.es.service.impl;

import cn.hutool.core.bean.BeanUtil;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.jeecg.modules.es.entity.Entry;
import org.jeecg.modules.es.service.EntryService;
import org.jeecg.modules.es.service.base.BaseElasticsearchService;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 词条服务实例
 *
 * @author dongjb
 * @version v1.0
 * @since 2021-09-04
 */
@Service
public class EntryServiceImpl extends BaseElasticsearchService implements EntryService {

    @Override
    public void createIndex(String index) {
        createIndexRequest(index);
    }

    @Override
    public void deleteIndex(String index) {
        deleteIndexRequest(index);
    }

    @Override
    public void insert(String index, List<Entry> list) {

        try {
            list.forEach(Entry -> {
                IndexRequest request = buildIndexRequest(index, String.valueOf(Entry.getEntryId()), Entry);
                try {
                    client.index(request, COMMON_OPTIONS);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void update(String index, List<Entry> list) {
        list.forEach(Entry -> updateRequest(index, String.valueOf(Entry.getEntryId()), Entry));
    }

    @Override
    public void delete(String index, Entry Entry) {
        if (ObjectUtils.isEmpty(Entry)) {
            // 如果Entry 对象为空，则删除全量
            searchAllList(index).forEach(p -> deleteRequest(index, String.valueOf(p.getEntryId())));
        }
        deleteRequest(index, String.valueOf(Entry.getEntryId()));
    }

    @Override
    public List<Entry> searchAllList(String index) {
        SearchResponse searchResponse = searchAll(index);
        SearchHit[] hits = searchResponse.getHits().getHits();
        List<Entry> EntryList = new ArrayList<>();
        Arrays.stream(hits).forEach(hit -> {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            Entry Entry = BeanUtil.mapToBean(sourceAsMap, Entry.class, false);
            EntryList.add(Entry);
        });
        return EntryList;
    }

    @Override
    public List<Entry> searchDataPageByDescriber(String index, String term, int startPage, int pageSize) {
        List<Map<String, Object>> list;
        List<Entry> entryList = new ArrayList<>();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //需要返回和不返回的字段，可以是数组也可以是字符串
        String[] fields = {"entryId", "entryTitle", "entryDesc", "keyword", "weight"};
        sourceBuilder.fetchSource(fields, null);
        //设置根据哪个字段进行排序
        sourceBuilder.sort(new FieldSortBuilder("weight").order(SortOrder.DESC));
        //添加查询条件
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        builder.must(QueryBuilders.matchQuery("entryDesc", term));
        //分页信息
        list = searchDataPage(index, startPage, pageSize, sourceBuilder, builder);
        list.forEach(source -> {
            Entry entry = BeanUtil.mapToBean(source, Entry.class, false);
            entryList.add(entry);
        });
        return entryList;
    }
}
