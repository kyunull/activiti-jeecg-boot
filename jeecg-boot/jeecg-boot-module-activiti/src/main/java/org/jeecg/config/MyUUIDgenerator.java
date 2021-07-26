package org.jeecg.config;

import cn.hutool.core.util.IdUtil;
import org.activiti.engine.impl.cfg.IdGenerator;

/**
 * id生成器
 *
 * @author: dongjb
 * @date: 2021/6/1
 */
public class MyUUIDgenerator implements IdGenerator {
    @Override
    public String getNextId() {
        return "act-" + IdUtil.randomUUID();
    }
}
