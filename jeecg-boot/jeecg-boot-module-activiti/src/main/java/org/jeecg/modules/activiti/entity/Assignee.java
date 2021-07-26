package org.jeecg.modules.activiti.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分配
 *
 * @author: dongjb
 * @date: 2021/6/1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Assignee {

    private String username;

    private Boolean isExecutor;
}
