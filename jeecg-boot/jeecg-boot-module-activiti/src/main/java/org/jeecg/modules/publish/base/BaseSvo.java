package org.jeecg.modules.publish.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Getter;
import lombok.Setter;

/**
 * 基础vo
 *
 * @author: dongjb
 * @date: 2021/6/7
 */
@Getter
@Setter
public class BaseSvo {
    private int pageNum = 1;
    private int pageSize = 10;

    public <T> Page<T> getPage() {
        return new Page<>(pageNum, pageSize);
    }
}
