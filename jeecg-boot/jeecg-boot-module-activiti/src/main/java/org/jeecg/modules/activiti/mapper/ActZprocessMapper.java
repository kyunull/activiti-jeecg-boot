package org.jeecg.modules.activiti.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.activiti.entity.ActZprocess;

import java.util.List;

/**
 * 流程定义扩展表
 *
 * @author: dongjb
 * @date: 2021/5/27
 */
public interface ActZprocessMapper extends BaseMapper<ActZprocess> {

    /**
     * 进程key最新版本
     *
     * @param processKey 进程key
     * @return 进程列表
     */
    List<ActZprocess> selectNewestProcess(@Param("processKey") String processKey);
}
