package org.jeecg.modules.chat.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Vector;

/**
 * 直播socket服务
 *
 * @author dongjb
 * @date 2021/08/25
 */
@Slf4j
@Component
@ServerEndpoint(value = "/chatroom")
public class ChatroomServer {

    private Session session;
    public static Vector<ChatroomServer> clients = new Vector<>();

    /**
     * 客户端建立连接时触发的事件
     *
     * @param session 会话
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        clients.add(this);
        System.out.print("新连接建立");
    }

    /**
     * 断开连接触发的事件
     */
    @OnClose
    public void onClose() {
        clients.remove(this);
        System.out.print("连接关闭了！");
    }

    /**
     * 客户端向服务器发送信息时触发的事件
     *
     * @param message 消息
     * @param session 会话
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("message {}", message);
        for (ChatroomServer client : clients) {
            try {
                client.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
