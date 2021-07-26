package org.jeecg.modules.publish.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.publish.entity.AppVersion;
import org.jeecg.modules.publish.service.AppVersionService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 应用版本控制类
 *
 * @author: dongjb
 * @date: 2021/6/6
 */
@Api(tags = "应用发布版本")
@AllArgsConstructor
@RestController
@RequestMapping("api/appVersions")
public class AppVersionController {
    private final AppVersionService appVersionService;

    @ApiOperation("编辑")
    @PutMapping
    public Result updateById(@RequestBody AppVersion appVersion) {
        appVersionService.updateById(appVersion);
        return Result.ok();
    }

    @ApiOperation("删除")
    @DeleteMapping("{id}")
    public void deleteById(@PathVariable Integer id) {
        appVersionService.removeById(id);
    }

    @ApiOperation("apk下载")
    @GetMapping("downloadApk/{versionId}")
    public ResponseEntity<Resource> downloadApk(@PathVariable Integer versionId) {
        return appVersionService.downloadApk(versionId);
    }
}
