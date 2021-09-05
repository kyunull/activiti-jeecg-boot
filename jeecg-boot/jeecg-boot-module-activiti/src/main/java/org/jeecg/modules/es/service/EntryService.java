package org.jeecg.modules.es.service;

import org.jeecg.modules.es.entity.Entry;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * 词条服务
 *
 * @author dongjb
 * @version v1.0
 * @since 2021-09-04
 */
public interface EntryService {

    /**
     * 创建索引库
     *
     * @param index 索引库名称
     */
    void createIndex(String index);

    /**
     * 删除索引库
     *
     * @param index 索引库名称
     */
    void deleteIndex(String index);

    /**
     * 插入索引记录
     *
     * @param index 索引库名称
     * @param list  索引记录列表
     */
    void insert(String index, List<Entry> list);

    /**
     * 更新索引记录
     *
     * @param index 索引库名称
     * @param list  索引记录列表
     */
    void update(String index, List<Entry> list);

    /**
     * 删除索引记录
     *
     * @param entry 索引记录对象
     */
    void delete(String index, @Nullable Entry entry);

    /**
     * 查询全部索引记录
     *
     * @param index 索引库名称
     * @return 索引记录对象列表
     */
    List<Entry> searchAllList(String index);

    /**
     * 根据条件查询索引记录，分页显示
     *
     * @param index 索引库名称
     * @return 索引记录对象列表
     */
    List<Entry> searchDataPageByDescriber(String index, String term, int startPage, int pageSize);

}
