package org.jeecg.modules.business.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.business.entity.OaArticle;
import org.jeecg.modules.business.service.IOaArticleService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

 /**
 * @Description: oa_article
 * @Author: jeecg-boot
 * @Date:   2021-07-29
 * @Version: V1.0
 */
@Api(tags="工作流业务-文章管理")
@RestController
@RequestMapping("/business/oaArticle")
@Slf4j
public class OaArticleController extends JeecgController<OaArticle, IOaArticleService> {
	@Autowired
	private IOaArticleService oaArticleService;
	
	/**
	 * 分页列表查询
	 *
	 * @param oaArticle
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "oa_article-分页列表查询")
	@ApiOperation(value="oa_article-分页列表查询", notes="oa_article-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(OaArticle oaArticle,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<OaArticle> queryWrapper = QueryGenerator.initQueryWrapper(oaArticle, req.getParameterMap());
		Page<OaArticle> page = new Page<OaArticle>(pageNo, pageSize);
		IPage<OaArticle> pageList = oaArticleService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param oaArticle
	 * @return
	 */
	@AutoLog(value = "oa_article-添加")
	@ApiOperation(value="oa_article-添加", notes="oa_article-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody OaArticle oaArticle) {
		oaArticleService.save(oaArticle);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param oaArticle
	 * @return
	 */
	@AutoLog(value = "oa_article-编辑")
	@ApiOperation(value="oa_article-编辑", notes="oa_article-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody OaArticle oaArticle) {
		oaArticleService.updateById(oaArticle);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "oa_article-通过id删除")
	@ApiOperation(value="oa_article-通过id删除", notes="oa_article-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		oaArticleService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "oa_article-批量删除")
	@ApiOperation(value="oa_article-批量删除", notes="oa_article-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.oaArticleService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "oa_article-通过id查询")
	@ApiOperation(value="oa_article-通过id查询", notes="oa_article-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		OaArticle oaArticle = oaArticleService.getById(id);
		if(oaArticle==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(oaArticle);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param oaArticle
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, OaArticle oaArticle) {
        return super.exportXls(request, oaArticle, OaArticle.class, "oa_article");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, OaArticle.class);
    }

}
