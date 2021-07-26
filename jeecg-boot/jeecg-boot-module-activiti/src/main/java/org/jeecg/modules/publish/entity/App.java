package org.jeecg.modules.publish.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.jeecg.modules.publish.base.BaseLogEntity;

/**
 * 应用实体类
 *
 * @author: dongjb
 * @date: 2021/6/7
 */
@ApiModel("应用列表")
@Getter
@Setter
@TableName("pub_app")
public class App extends BaseLogEntity {

    @ApiModelProperty("应用名称")
    private String name;

    @ApiModelProperty("包名")
    private String packageName;

    @ApiModelProperty("短链接")
    private String shortCode;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("当前的版本id")
    private Integer currentVersionId;

    @ApiModelProperty("ios地址")
    private String iosUrl;

}
