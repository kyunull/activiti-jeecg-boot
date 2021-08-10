package org.jeecg.modules.business.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: oa_article
 * @Author: jeecg-boot
 * @Date:   2021-07-29
 * @Version: V1.0
 */
@Data
@TableName("oa_article")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="oa_article对象", description="oa_article")
public class OaArticle implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    private java.lang.Integer id;
	/**文章分类：1政府重要通知；2政策法规；3内部刊物；4资料管理；5新闻；6公告；*/
	@Excel(name = "文章分类：1政府重要通知；2政策法规；3内部刊物；4资料管理；5新闻；6公告；", width = 15)
    @ApiModelProperty(value = "文章分类：1政府重要通知；2政策法规；3内部刊物；4资料管理；5新闻；6公告；")
    private java.lang.String articleCategory;
	/**文章标题*/
	@Excel(name = "文章标题", width = 15)
    @ApiModelProperty(value = "文章标题")
    private java.lang.String articleTitle;
	/**封面图链接*/
	@Excel(name = "封面图链接", width = 15)
    @ApiModelProperty(value = "封面图链接")
    private java.lang.String coverImgUrl;
	/**关键字*/
	@Excel(name = "关键字", width = 15)
    @ApiModelProperty(value = "关键字")
    private java.lang.String articleKeywords;
	/**文章内容*/
	@Excel(name = "文章内容", width = 15)
    @ApiModelProperty(value = "文章内容")
    private java.lang.String articleContent;
	/**文章类型：1原创2转载*/
	@Excel(name = "文章类型：1原创2转载", width = 15)
    @ApiModelProperty(value = "文章类型：1原创2转载")
    private java.lang.String articleType;
	/**转载原链接*/
	@Excel(name = "转载原链接", width = 15)
    @ApiModelProperty(value = "转载原链接")
    private java.lang.String transferUrl;
	/**位置类型：1普通2热门3置顶4滚动*/
	@Excel(name = "位置类型：1普通2热门3置顶4滚动", width = 15)
    @ApiModelProperty(value = "位置类型：1普通2热门3置顶4滚动")
    private java.lang.String positionType;
	/**文章浏览量*/
	@Excel(name = "文章浏览量", width = 15)
    @ApiModelProperty(value = "文章浏览量")
    private java.lang.Integer articleViews;
	/**显示类型：0隐藏；1显示；*/
	@Excel(name = "显示类型：0隐藏；1显示；", width = 15)
    @ApiModelProperty(value = "显示类型：0隐藏；1显示；")
    private java.lang.String showType;
	/**文章排序*/
	@Excel(name = "文章排序", width = 15)
    @ApiModelProperty(value = "文章排序")
    private java.lang.String articleSort;
	/**创建者*/
    @ApiModelProperty(value = "创建者")
    private java.lang.String createBy;
	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "创建时间")
    private java.util.Date createTime;
	/**更新者*/
    @ApiModelProperty(value = "更新者")
    private java.lang.String updateBy;
	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "更新时间")
    private java.util.Date updateTime;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
    private java.lang.String remarks;
	/**删除标记 0-有效  1-删除*/
	@Excel(name = "删除标记 0-有效  1-删除", width = 15)
    @ApiModelProperty(value = "删除标记 0-有效  1-删除")
    private java.lang.String delFlag;
}
