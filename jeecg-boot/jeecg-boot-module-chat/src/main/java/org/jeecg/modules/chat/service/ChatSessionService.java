package org.jeecg.modules.chat.service;

import org.jeecg.modules.chat.entity.Message;

import java.util.List;

/**
 * 服务
 *
 * @author dongjb
 * @date 2021/08/17
 */
public interface ChatSessionService {

    /**
     * 推送消息，储存到Redis数据库中
     *
     * @param fromId  推送方ID
     * @param toId    接收方ID
     * @param message 消息
     */
    void pushMessage(String fromId, String fromImg, String fromName, String toId, String toImg, String toName, String message);

    /**
     * 获取公共消息内容 -- 群组
     *
     * @return 群组消息内容
     */
    List<Message> commonList();

    /**
     * 获取该用户与指定窗口的推送消息
     *
     * @param fromId 推送方ID
     * @param toId   接收方ID
     * @return 消息列表
     */
    List<Message> selfList(String fromId, String toId);

}
