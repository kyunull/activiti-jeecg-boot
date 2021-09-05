package org.jeecg;

import org.jeecg.modules.es.config.ElasticsearchConstant;
import org.jeecg.modules.es.entity.Entry;
import org.jeecg.modules.es.service.EntryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EntryTests {

    @Autowired
    private EntryService entryService;

    /**
     * 测试删除索引
     */
    @Test
    public void deleteIndexTest() {
        entryService.deleteIndex(ElasticsearchConstant.INDEX_NAME);
    }

    /**
     * 测试创建索引
     */
    @Test
    public void createIndexTest() {
        entryService.createIndex(ElasticsearchConstant.INDEX_NAME);
    }

    /**
     * 测试新增
     */
    @Test
    public void insertTest() {
        List<Entry> list = new ArrayList<>();
        list.add(Entry.builder().entryId("1").entryTitle("腾讯军事频道 - 腾讯首页").entryDesc("腾讯网从2003年创立至今,已经成为集新闻信息,区域垂直生活服务、社会化媒体资讯和产品为一体的互联网媒体平台。腾讯网下设新闻、科技、财经、娱乐、体育、汽车、时尚等多个频道,充分满足用户对不").keyword("一旦开战最安全的省中国今日军事头条铁血军事网今日7点凌晨中日激战").weight(7).build());
        list.add(Entry.builder().entryId("2").entryTitle("铁血军事网").entryDesc("军事 国际 历史 社会 美食 我军新班用机枪露真容,终于可以用弹链“泼水”了(图)。 11月09日 人类有史以来最短命的航母——鬼子的信浓号航母 11月04日 中国陆军大规模采购军用防弹插板,单兵防护").keyword("台海形势、中国军情、国际军情、军事专题、网友原").weight(4).build());
        list.add(Entry.builder().entryId("3").entryTitle("军事_中国网_权威军事新闻网站").entryDesc("中国网军事频道是中国最权威的军事网站之一,报道中国、国际等全球军事新闻。主要有以下栏目:军方发布、中国军事、国际军事、军情24小时、武器库、论战、军史、军事图库、军事论坛").keyword("大陆是否会开战近日台湾陆委会前副主委赵建民就表示").weight(5).build());
        list.add(Entry.builder().entryId("4").entryTitle("军事").entryDesc("《史记·律书》：“会高祖厌苦军事，亦有萧张之谋，故偃武一休息，羁縻不备。” 唐柳宗元《祭李中丞文》：“发迹内史，参其军事。” 《东周列国志》第七回：“庄公大犒三军，临别与夷仲年、公子翚刑牲而盟：‘三国同患相恤。后有军事，各出兵车为助。如背此言，神明不宥").keyword("与军队或战争有关的事情").weight(2).build());

        entryService.insert(ElasticsearchConstant.INDEX_NAME, list);
    }

    /**
     * 测试更新
     */
    @Test
    public void updateTest() {
        Entry entry = Entry.builder().entryId("1").remark("test3_update").build();
        List<Entry> list = new ArrayList<>();
        list.add(entry);
        entryService.update(ElasticsearchConstant.INDEX_NAME, list);
    }

    /**
     * 测试删除
     */
    @Test
    public void deleteTest() {
        entryService.delete(ElasticsearchConstant.INDEX_NAME, Entry.builder().entryId("1").build());
    }

    /**
     * 测试查询
     */
    @Test
    public void searchListTest() {
        List<Entry> entryList = entryService.searchAllList(ElasticsearchConstant.INDEX_NAME);
        System.out.println(entryList);
    }

    /**
     * 测试分页查询
     */
    @Test
    public void searchDatePageTest() {
        List<Entry> entryList = entryService.searchDataPageByDescriber(ElasticsearchConstant.INDEX_NAME, "中国", 1, 10);
        System.out.println(entryList);
    }

}
