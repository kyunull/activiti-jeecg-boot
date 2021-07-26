package org.jeecg.modules.publish.vo;

import lombok.Getter;
import lombok.Setter;
import org.jeecg.modules.publish.entity.AppVersion;

/**
 * 应用上传vo
 *
 * @author: dongjb
 * @date: 2021/6/7
 */
@Getter
@Setter
public class AppUploadVo extends AppVersion {
    private String packageName;
}
