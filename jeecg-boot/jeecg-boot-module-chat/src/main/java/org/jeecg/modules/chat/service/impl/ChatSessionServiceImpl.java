package org.jeecg.modules.chat.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.chat.constant.CommonConstant;
import org.jeecg.modules.chat.entity.Message;
import org.jeecg.modules.chat.service.ChatSessionService;
import org.jeecg.modules.chat.utils.CoreUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 服务实现
 *
 * @author dongjb
 * @date 2021/08/17
 */
@Slf4j
@Service
public class ChatSessionServiceImpl implements ChatSessionService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void pushMessage(String fromId, String fromImg, String fromName, String toId, String toImg, String toName, String message) {
        Message entity = new Message();
        entity.setMessage(message);
        entity.setFrom(fromId);
        entity.setFromImg(fromImg);
        entity.setFromName(fromName);
        entity.setTime(CoreUtil.format(new Date()));
        if (!toId.equals("0")) {
            //查询接收方信息
            entity.setTo(toId);
            entity.setToImg(toImg);
            entity.setToName(toName);
            //单个用户推送
            push(entity, CommonConstant.CHAT_FROM_PREFIX + fromId + CommonConstant.CHAT_TO_PREFIX + toId);
        } else {
            //公共消息 -- 群组
            entity.setTo(null);
            entity.setToImg(null);
            entity.setToName(null);
            push(entity, CommonConstant.CHAT_COMMON_PREFIX + fromId);
        }
    }

    /**
     * 推送消息
     *
     * @param entity Session value
     * @param key    Session key
     */
    private void push(Message entity, String key) {
        //这里按照 PREFIX_ID 格式，作为KEY储存消息记录
        //但一个用户可能推送很多消息，VALUE应该是数组
        List<Message> list = new ArrayList<>();
        String value = redisTemplate.boundValueOps(key).get();
        //非第一次推送消息
        if (value != null) {
            list = Objects.requireNonNull(JSONObject.parseArray(value)).toJavaList(Message.class);
        }
        list.add(entity);
        redisTemplate.boundValueOps(key).set(JSONObject.toJSONString(list));
    }

    @Override
    public List<Message> commonList() {
        List<Message> list = new ArrayList<>();
        Set<String> keys = redisTemplate.keys(CommonConstant.CHAT_COMMON_PREFIX + CommonConstant.REDIS_MATCH_PREFIX);
        if (keys != null && keys.size() > 0) {
            keys.forEach(key -> {
                String value = redisTemplate.boundValueOps(key).get();
                List<Message> messageList = Objects.requireNonNull(JSONObject.parseArray(value)).toJavaList(Message.class);
                list.addAll(messageList);
            });
        }
        CoreUtil.sort(list);
        return list;
    }

    @Override
    public List<Message> selfList(String fromId, String toId) {
        List<Message> list = new ArrayList<>();
        //A -> B
        String fromTo = redisTemplate.boundValueOps(CommonConstant.CHAT_FROM_PREFIX + fromId + CommonConstant.CHAT_TO_PREFIX + toId).get();
        //B -> A
        String toFrom = redisTemplate.boundValueOps(CommonConstant.CHAT_FROM_PREFIX + toId + CommonConstant.CHAT_TO_PREFIX + fromId).get();

        JSONArray fromToObject = JSONObject.parseArray(fromTo);
        JSONArray toFromObject = JSONObject.parseArray(toFrom);
        if (fromToObject != null) {
            list.addAll(fromToObject.toJavaList(Message.class));
        }
        if (toFromObject != null) {
            list.addAll(toFromObject.toJavaList(Message.class));
        }

        if (list.size() > 0) {
            CoreUtil.sort(list);
            return list;
        } else {
            return new ArrayList<>();
        }
    }

}
