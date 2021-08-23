package org.jeecg.modules.chat.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.chat.entity.Message;
import org.jeecg.modules.chat.exception.GlobalException;
import org.jeecg.modules.chat.service.ChatSessionService;
import org.jeecg.modules.chat.utils.CoreUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * websocket接入点
 *
 * @author dongjb
 * @date 2021/08/17
 */
@Slf4j
@Component
@ServerEndpoint(value = "/chat/{id}")
public class WebsocketServerEndpoint {

    private static ChatSessionService chatSessionService;

    @Autowired
    public void setChatSessionService(ChatSessionService chatSessionService) {
        WebsocketServerEndpoint.chatSessionService = chatSessionService;
    }

    //在线连接数
    private static long online = 0;

    //用于存放当前Websocket对象的Set集合
    private static final CopyOnWriteArraySet<WebsocketServerEndpoint> websocketServerEndpoints = new CopyOnWriteArraySet<>();

    //与客户端的会话Session
    private Session session;

    //当前会话窗口ID
    private String fromId = "";

    /**
     * 链接成功调用的方法
     *
     * @param session session
     * @param id      用户标识
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("id") String id) {
        log.info("onOpen >> 链接成功");
        this.session = session;

        //将当前websocket对象存入到Set集合中
        websocketServerEndpoints.add(this);

        //在线人数+1
        addOnlineCount();

        log.info("有新窗口开始监听：" + id + ", 当前在线人数为：" + getOnlineCount());

        this.fromId = id;
        try {
            //群发消息
            Map<String, Object> map = new HashMap<>();
            map.put("msg", "用户 " + id + " 已上线");
            sendMore(JSONObject.toJSONString(map));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 链接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        log.info("onClose >> 链接关闭");

        //移除当前Websocket对象
        websocketServerEndpoints.remove(this);

        //在内线人数-1
        subOnLineCount();

        log.info("链接关闭，当前在线人数：" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 信息
     */
    @OnMessage
    public void onMessage(String message) {
//        log.info("接收到窗口：" + fromId + " 的信息：" + message);
//        chatSessionService.pushMessage(fromId, null, message);
//
//        //群发消息
//        sendMore(getData(null, message));
    }

    @OnError
    public void onError(Throwable e) {
        e.printStackTrace();
    }

    /**
     * 推送消息
     *
     * @param message 信息
     */
    private void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 封装返回消息
     *
     * @param toId    指定窗口ID
     * @param message 消息内容
     * @return 消息json字符串
     */
    private String getData(String fromId, String fromImg, String fromName, String toId, String toImg, String toName, String message) {
        Message entity = new Message();
        entity.setMessage(message);
        entity.setTime(CoreUtil.format(new Date()));
        entity.setFrom(fromId);
        entity.setFromImg(fromImg);
        entity.setFromName(fromName);
        entity.setTo(toId);
        entity.setToImg(toImg);
        entity.setToName(toName);
        return JSONObject.toJSONString(entity);
    }

    /**
     * 群发消息
     *
     * @param data 待发送数据
     */
    private void sendMore(String data) {
        for (WebsocketServerEndpoint websocketServerEndpoint : websocketServerEndpoints) {
            try {
                websocketServerEndpoint.sendMessage(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 指定窗口推送消息
     *
     * @param toId    接收者
     * @param fromId  推送者
     * @param content 推送内容
     */
    public void sendTo(String fromId, String fromImg, String fromName, String toId, String toImg, String toName, String content) {
        this.fromId = fromId;
        if (websocketServerEndpoints.size() <= 1) {
            throw new GlobalException("用户未上线");
        }
        boolean flag = false;
        for (WebsocketServerEndpoint endpoint : websocketServerEndpoints) {
            try {
                if (endpoint.fromId.equals(toId)) {
                    flag = true;
                    log.info(fromId + " 推送消息到窗口：" + toId + " ，推送内容：" + content);

                    endpoint.sendMessage(getData(fromId, fromImg, fromName, toId, toImg, toName, content));
                    chatSessionService.pushMessage(fromId, fromImg, fromName, toId, toImg, toName, content);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!flag) {
            if (toId.equals("0")) {
                chatSessionService.pushMessage(fromId, fromImg, fromName, toId, toImg, toName, content);
                sendMore(getData(fromId, fromImg, fromName, toId, toImg, toName, content));
            } else {
                throw new GlobalException("推送失败，找不到该窗口");
            }
        }
    }

    private void subOnLineCount() {
        WebsocketServerEndpoint.online--;
    }

    private synchronized long getOnlineCount() {
        return online;
    }

    private void addOnlineCount() {
        WebsocketServerEndpoint.online++;
    }
}
