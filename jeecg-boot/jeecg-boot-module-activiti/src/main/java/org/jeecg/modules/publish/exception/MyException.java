package org.jeecg.modules.publish.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * 我的异常类
 *
 * @author: dongjb
 * @date: 2021/6/7
 */
public class MyException extends ResponseStatusException {

    public MyException() {
        super(HttpStatus.BAD_REQUEST);
    }

    public MyException(String reason) {
        super(HttpStatus.BAD_REQUEST, reason == null ? "" : reason);
    }

    public MyException(HttpStatus status, String reason) {
        super(status, reason);
    }

    public MyException(int status, String reason) {
        super(HttpStatus.valueOf(status), reason);
    }

    public MyException(Throwable t) {
        super(HttpStatus.BAD_REQUEST, t.getMessage(), t);
    }
}
