package org.jeecg.modules.activiti.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.activiti.entity.ActZprocess;

import java.util.List;

/**
 * 流程定义扩展表
 *
 * @author: dongjb
 * @date: 2021/5/27
 */
public interface IActZprocessService extends IService<ActZprocess> {

    List<ActZprocess> queryNewestProcess(String processKey);
}
