package org.jeecg.modules.chat.service;

import org.jeecg.modules.chat.controller.WebrtcWS;
import org.jeecg.modules.chat.entity.WebrtcMessage;
import org.springframework.stereotype.Service;

/**
 * 房间管理服务
 *
 * @author dongjb
 * @date 2021/09/07
 * @since 1.0.0
 */
@Service
public interface WebrtcRoomService {
    /**
     * 查询指定房间存在人数
     *
     * @param roomId 房间ID
     * @return 房间人数
     */
    int countOfUserInRoom(String roomId);

    /**
     * 踢出用户
     *
     * @param roomId 房间ID
     * @param userId userId
     * @return 成功true，失败false
     */
    boolean kickUser(String roomId, String userId);

    /**
     * @param roomId   房间ID
     * @param webrtcWS websocket连接
     * @param token    用户token,验证身份用
     * @param roomPw   房间密码
     * @return 成功true，失败false
     */
    boolean enterRoom(String roomId, String roomPw, String token, WebrtcWS webrtcWS, String nickname) throws Exception;

    /**
     * @param roomId   房间ID
     * @param webrtcWS websocket连接
     * @param token    用户token,验证身份用
     * @param roomPw   房间密码
     * @return 成功true，失败false
     */
    boolean createRoom(String roomId, String roomPw, String token, WebrtcWS webrtcWS, String nickname) throws Exception;

    /**
     * @param roomId 房间ID
     * @param userId 用户ID
     * @return 成功true，失败false
     */
    boolean userLeave(String roomId, String userId);

    /**
     * 获取房间内所有人
     *
     * @param roomId 房间id
     * @return 所有人
     */
    String getRoomUsers(String roomId);

    /**
     * 转发消息到房间所有人
     *
     * @param roomId        房间Id
     * @param message       要转发的消息
     * @param excludeUserId 不转发的userId
     * @return 是否成功
     */
    boolean forwardToEveryoneInRoom(String roomId, WebrtcMessage message, String excludeUserId);

    /**
     * 转发消息到指定人
     *
     * @param roomId  房间Id
     * @param message 要转发的消息
     * @param userId  转发的userId
     * @return 是否成功
     */
    boolean forwardToOneInRoom(String roomId, WebrtcMessage message, String userId);
}
