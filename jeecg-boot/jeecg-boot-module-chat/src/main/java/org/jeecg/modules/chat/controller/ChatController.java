package org.jeecg.modules.chat.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.chat.entity.Message;
import org.jeecg.modules.chat.exception.GlobalException;
import org.jeecg.modules.chat.service.ChatSessionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author dongjb
 * @date 2021/08/17
 */
@Api(tags = "即时通信")
@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatSessionService chatSessionService;

    public ChatController(ChatSessionService chatSessionService) {
        this.chatSessionService = chatSessionService;
    }

    /**
     * 向指定窗口推送消息
     *
     * @param fromUserId 发送方ID
     * @param toUserId   接收方ID
     * @param message    消息
     * @return 通用返回对象
     */
    @ApiOperation(value = "向指定窗口推送消息", notes = "向指定窗口推送消息")
    @GetMapping("/push")
    public Result push(@RequestParam String fromUserId,
                       @RequestParam String fromUserImg,
                       @RequestParam String fromUserName,
                       @RequestParam String toUserId,
                       @RequestParam String toUserImg,
                       @RequestParam String toUserName,
                       @RequestParam String message) {
        try {
            WebsocketServerEndpoint endpoint = new WebsocketServerEndpoint();
            endpoint.sendTo(fromUserId, fromUserImg, fromUserName, toUserId, toUserImg, toUserName, message);
            return Result.OK();
        } catch (GlobalException e) {
            e.printStackTrace();
            return Result.error(500, e.getMsg());
        }
    }

    /**
     * 获取公共聊天消息内容
     *
     * @return 公共聊天内容列表
     */
    @ApiOperation(value = "获取公共聊天消息内容", notes = "获取公共聊天消息内容")
    @GetMapping("/common")
    public Result<List<Message>> commonList() {
        return Result.OK(chatSessionService.commonList());
    }

    /**
     * 获取指定用户的聊天消息内容
     *
     * @param fromId 该用户ID
     * @param toId   哪个窗口
     * @return 指定用户的聊天消息
     */
    @ApiOperation(value = "获取指定用户的聊天消息内容", notes = "获取指定用户的聊天消息内容")
    @GetMapping("/self/{fromId}/{toId}")
    public Result<List<Message>> selfList(@PathVariable("fromId") String fromId, @PathVariable("toId") String toId) {
        List<Message> list = chatSessionService.selfList(fromId, toId);
        return Result.OK(list);
    }

}
