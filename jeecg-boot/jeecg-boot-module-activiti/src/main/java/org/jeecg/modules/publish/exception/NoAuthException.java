package org.jeecg.modules.publish.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * 无权限异常
 *
 * @author: dongjb
 * @date: 2021/6/7
 */
public class NoAuthException extends ResponseStatusException {
    public NoAuthException() {
        super(HttpStatus.FORBIDDEN, "无权限");
    }
}
