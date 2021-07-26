package org.jeecg.modules.publish.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * 认证异常类
 *
 * @author: dongjb
 * @date: 2021/6/7
 */
public class UnauthorizedException extends ResponseStatusException {
    public UnauthorizedException() {
        super(HttpStatus.UNAUTHORIZED, "未认证或者认证过期，请重新登录");
    }
}
