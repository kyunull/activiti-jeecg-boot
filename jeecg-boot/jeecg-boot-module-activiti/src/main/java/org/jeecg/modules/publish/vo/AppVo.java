package org.jeecg.modules.publish.vo;

import lombok.Getter;
import lombok.Setter;
import org.jeecg.modules.publish.entity.App;
import org.jeecg.modules.publish.entity.AppVersion;

import java.util.List;

/**
 * app vo
 *
 * @author: dongjb
 * @date: 2021/6/6
 */
@Getter
@Setter
public class AppVo extends App {
    private AppVersion currentVersion;
    private List<AppVersion> versions;
    private int downloadCount;
}
