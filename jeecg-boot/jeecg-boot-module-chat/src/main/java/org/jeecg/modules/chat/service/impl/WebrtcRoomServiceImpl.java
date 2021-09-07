package org.jeecg.modules.chat.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.chat.controller.WebrtcWS;
import org.jeecg.modules.chat.entity.WebrtcMessage;
import org.jeecg.modules.chat.service.WebrtcRoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 房间管理服务实现
 *
 * @author dongjb
 * @date 2021/09/07
 * @since 1.0.0
 */
@Service
public class WebrtcRoomServiceImpl implements WebrtcRoomService {
    @Autowired
    private ISysBaseAPI sysBaseAPI;
    private final static Logger logger = LoggerFactory.getLogger(WebrtcRoomServiceImpl.class);
    private final Map<String, Map<String, WebrtcWS>> rooms = new ConcurrentHashMap<>();
    private final Map<String, String> roomsPw = new ConcurrentHashMap<>();

    @Override
    public int countOfUserInRoom(String roomId) {
        Map<String, WebrtcWS> room = rooms.get(roomId);
        if (room == null) {
            return 0;
        } else {
            return room.size();
        }
    }

    @Override
    public boolean kickUser(String roomId, String userId) {
        try {
            Map<String, WebrtcWS> room = rooms.get(roomId);
            if (room.size() > 0) {
                room.remove(userId);
                logger.info("用户:" + userId + "被移除,房间:" + roomId + "\n房间内现有用户: " + getRoomUsers(roomId));
            }
            if (room.size() == 0) {
                rooms.remove(roomId);
                logger.info("房间:" + roomId + " 没有人，房间被移除");
            }
        } catch (Exception exception) {
            logger.info("无法提出用户" + exception.getMessage());
        }
        return true;
    }

    @Override
    public boolean enterRoom(String roomId, String roomPw, String token, WebrtcWS webrtcWS, String nickname) throws Exception {
//        String userId = String.valueOf(new JwtTokenUtil().getUserIdFromToken(token));
        //获取登录用户信息
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String userId = sysUser.getId();
        if (userId.equals("0")) {
            throw new Exception("身份验证失败");
        } else {
            webrtcWS.setUserId(userId);
        }
        Map<String, WebrtcWS> room = rooms.get(roomId);
        if (room == null) {
            throw new Exception("房间不存在");
        } else {
            if (roomsPw.get(roomId).equals(roomPw)) {
//                userService.update(new User(Long.parseLong(userId), nickname), new QueryWrapper<User>().lambda().eq(User::getId, userId));
                webrtcWS.setRoomId(roomId);
                room.put(userId, webrtcWS);
                webrtcWS.getSession().getBasicRemote().sendText(new ObjectMapper().writeValueAsString(new WebrtcMessage(WebrtcMessage.TYPE_COMMAND_SUCCESS, userId, roomId, "enter")));
                logger.info("用户:" + webrtcWS.getUserId() + "进入房间:" + roomId + "\n房间有: " + getRoomUsers(roomId));
                return true;
            } else {
                throw new Exception("密码错误");
            }
        }
    }

    @Override
    public boolean createRoom(String roomId, String roomPw, String token, WebrtcWS webrtcWS, String nickname) throws Exception {
        //获取登录用户信息
        LoginUser sysUser = getUserInfoByToken(token);
        String userId = sysUser.getId();
        if (userId.equals("0")) {
            throw new Exception("身份验证失败");
        } else {
            webrtcWS.setUserId(userId);
        }
        Map<String, WebrtcWS> room = rooms.get(roomId);
        if (room == null) {
//            userService.update(new User(Long.parseLong(userId), nickname), new QueryWrapper<User>().lambda().eq(User::getId, userId));
            Map<String, WebrtcWS> newRoom = new ConcurrentHashMap<>();
            //把自己创建人添加到房间
            webrtcWS.setRoomId(roomId);
            newRoom.put(userId, webrtcWS);
            //把房间加到房间map
            rooms.put(roomId, newRoom);
            //设置房间密码
            roomsPw.put(roomId, roomPw);
            webrtcWS.getSession().getBasicRemote().sendText(new ObjectMapper().writeValueAsString(new WebrtcMessage(WebrtcMessage.TYPE_COMMAND_SUCCESS, userId, roomId, "create")));
            logger.info("用户:" + webrtcWS.getUserId() + "创建房间:" + roomId + "\n房间有: " + getRoomUsers(roomId));
            return true;
        } else {
            throw new Exception("房间已经存在");
        }
    }

    @Override
    public boolean userLeave(String roomId, String userId) {
        Map<String, WebrtcWS> room = rooms.get("roomId");
        room.remove(userId);
        return false;
    }

    @Override
    public String getRoomUsers(String roomId) {
        Map<String, WebrtcWS> room = rooms.get(roomId);
        StringBuffer stringBuffer = new StringBuffer();
        if (room.size() > 0) {
            room.forEach((k, v) -> stringBuffer.append(k).append("\n"));
        }
        return stringBuffer.toString();
    }

    @Override
    public boolean forwardToEveryoneInRoom(String roomId, WebrtcMessage message, String excludeUserId) {
        Map<String, WebrtcWS> room = rooms.get(roomId);
        try {
            final String msg = new ObjectMapper().writeValueAsString(message);
            room.forEach((k, v) -> {
                if (!k.equals(excludeUserId)) {
                    try {
                        v.getSession().getBasicRemote().sendText(msg);
                        logger.info("转发:" + msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean forwardToOneInRoom(String roomId, WebrtcMessage message, String userId) {
        try {
            String msg = new ObjectMapper().writeValueAsString(message);
            rooms.get(roomId).get(userId).getSession().getBasicRemote().sendText(msg);
            logger.info("转发到:" + userId + " ,内容: " + msg);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public LoginUser getUserInfoByToken(String token) {
        // 解密获得username，用于和数据库进行对比
        String username = JwtUtil.getUsername(token);
        if (username == null) {
            throw new AuthenticationException("Token非法无效!");
        }

        // 查询用户信息
        LoginUser user = sysBaseAPI.getUserByName(username);
        if (user == null) {
            throw new AuthenticationException("用户不存在!");
        }
        // 判断用户状态
        if (user.getStatus() != 1) {
            throw new AuthenticationException("账号已锁定,请联系管理员!");
        }
        return user;
    }

}
