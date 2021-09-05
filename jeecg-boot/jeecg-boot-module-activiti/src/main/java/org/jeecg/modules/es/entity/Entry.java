package org.jeecg.modules.es.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Entry
 *
 * @author dongjb
 * @version v1.0
 * @since 2021-09-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Entry implements Serializable {

    private static final long serialVersionUID = -8076851446078143653L;
    /**
     * 主键
     */
    private String entryId;

    /**
     * 词条标题
     */
    private String entryTitle;

    /**
     * 词条简介
     */
    private String entryDesc;

    /**
     * 图片。多个以逗号分隔
     */
    private String imgIds;

    /**
     * 类型
     */
    private String entryType;

    /**
     * 状态：在用1，失效0
     */
    private String entryStatus;

    /**
     * 关键字。多个以逗号分隔
     */
    private String keyword;

    /**
     * 权重（数字）
     */
    private Integer weight;

    /**
     * 创建人
     */
    private String createStaffId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人
     */
    private String updateStaffId;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 备注
     */
    private String remark;

}
