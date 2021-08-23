package org.jeecg.modules.chat.utils;

import org.jeecg.modules.chat.entity.Message;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 工具类
 *
 * @author dongjb
 * @date 2021/08/17
 */
public class CoreUtil {

    /**
     * 对List集合中的数据按照时间顺序排序
     *
     * @param list List<Message>
     */
    public static void sort(List<Message> list) {
        list.sort(Comparator.comparing(Message::getTime));
    }

    /**
     * 格式化日期
     *
     * @param date 需格式化的日期
     * @return 日期字符串
     */
    public static String format(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }
}
