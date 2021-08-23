package org.jeecg.modules.chat.entity;

import lombok.Data;
import org.jeecg.common.system.vo.LoginUser;

import java.io.Serializable;

/**
 * 会话消息实体
 *
 * @author dongjb
 * @date 2021/08/17
 */
@Data
public class Message {

    /**
     * 消息推送者
     */
    private String from;

    /**
     * 消息推送者头像
     */
    private String fromImg;

    /**
     * 消息推送者名称
     */
    private String fromName;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 消息接收者：
     * 如果是私有（向指定窗口推送），to即为接受者User对象
     * 如果是公共消息（群组聊天），to设为null
     */
    private String to;

    /**
     * 消息接收者头像
     */
    private String toImg;

    /**
     * 消息接收者名称
     */
    private String toName;

    /**
     * 创建时间
     */
    private String time;

    public void setMessage(String message) {
        this.message = message == null ? "" : message.replaceAll("\r\n|\r|\n", "");
    }
}
