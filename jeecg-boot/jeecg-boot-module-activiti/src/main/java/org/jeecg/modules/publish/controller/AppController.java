package org.jeecg.modules.publish.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.publish.entity.App;
import org.jeecg.modules.publish.entity.AppVersion;
import org.jeecg.modules.publish.service.AppService;
import org.jeecg.modules.publish.vo.AppUploadVo;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 应用控制类
 *
 * @author: dongjb
 * @date: 2021/6/6
 */
@Api(tags = "应用发布")
@AllArgsConstructor
@RestController
@RequestMapping("api/apps")
@Slf4j
public class AppController {

    private final AppService appService;

    @ApiOperation("获取所有应用")
    @GetMapping
    public Result getList() {
        return Result.ok(appService.getList());
    }

    @ApiOperation("根据id获取详情")
    @GetMapping("{id}")
    public Result selectById(@PathVariable Integer id) {
        return Result.ok(appService.selectById(id));
    }

    @ApiOperation("根据shortCode获取详情")
    @GetMapping("shortCode/{shortCode}")
    public Result selectByShortCode(@PathVariable String shortCode) {
        return Result.ok(appService.selectByShortCode(shortCode));
    }

    @ApiOperation("编辑")
    @PutMapping
    public Result updateById(@RequestBody App app) {
        appService.updateById(app);
        return Result.ok();
    }

    @ApiOperation("删除")
    @DeleteMapping("{id}")
    public void deleteById(@PathVariable Integer id) {
        appService.removeById(id);
    }

    @ApiOperation("apk上传")
    @PostMapping("upload")
    public void uploadApk(AppUploadVo appUploadVo, @RequestParam MultipartFile file) {
        appService.uploadApk(appUploadVo, file);
    }

    @ApiOperation("根据包名获取最新版本")
    @GetMapping("latest/{packageName}")
    public AppVersion selectLatestByPackageName(@PathVariable String packageName) {
        return appService.selectLatestByPackageName(packageName);
    }
}
